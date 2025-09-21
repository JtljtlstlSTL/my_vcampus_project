package com.vcampus.server.core.library.service.impl;

import com.vcampus.server.core.library.dao.BookDao;
import com.vcampus.server.core.library.dao.BookBorrowDao;
import com.vcampus.server.core.library.entity.core.Book;
import com.vcampus.server.core.library.entity.result.BookManagementResult;
import com.vcampus.server.core.library.entity.result.ExcelImportResult;
import com.vcampus.server.core.library.entity.search.PopularBook;
import com.vcampus.server.core.library.enums.BookStatus;
import com.vcampus.server.core.library.service.BookManagementService;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * 图书管理服务实现类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class BookManagementServiceImpl implements BookManagementService {
    
    private final BookDao bookDao;
    private final BookBorrowDao bookBorrowDao;
    
    public BookManagementServiceImpl() {
        this.bookDao = BookDao.getInstance();
        this.bookBorrowDao = BookBorrowDao.getInstance();
    }
    
    // ==================== 基础CRUD操作 ====================
    
    @Override
    public BookManagementResult addBook(Book book) {
        log.info("开始添加图书: title={}, isbn={}", book.getTitle(), book.getIsbn());
        
        try {
            // 1. 数据验证
            BookManagementResult validationResult = validateBook(book);
            if (!validationResult.isSuccess()) {
                return validationResult;
            }
            
            // 2. 检查ISBN是否已存在
            if (isIsbnExists(book.getIsbn(), null)) {
                return BookManagementResult.failure("ISBN已存在: " + book.getIsbn());
            }
            
            // 3. 设置默认值
            if (book.getStatus() == null) {
                book.setStatus(BookStatus.IN_LIBRARY);
            }
            if (book.getAddTime() == null) {
                book.setAddTime(LocalDateTime.now());
            }
            if (book.getUpdateTime() == null) {
                book.setUpdateTime(LocalDateTime.now());
            }
            if (book.getAvailQty() == null) {
                book.setAvailQty(book.getTotalQty() != null ? book.getTotalQty() : 1);
            }
            
            // 4. 保存图书
            Book savedBook = bookDao.save(book);
            
            log.info("图书添加成功: bookId={}, title={}", savedBook.getBookId(), savedBook.getTitle());
            return BookManagementResult.success("图书添加成功", savedBook);
            
        } catch (Exception e) {
            log.error("添加图书异常: title={}, isbn={}", book.getTitle(), book.getIsbn(), e);
            return BookManagementResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    @Override
    public BookManagementResult updateBook(Book book) {
        log.info("开始更新图书: bookId={}, title={}", book.getBookId(), book.getTitle());
        
        try {
            // 1. 参数验证
            if (book.getBookId() == null) {
                return BookManagementResult.failure("图书ID不能为空");
            }
            
            // 2. 检查图书是否存在
            Optional<Book> existingBookOpt = bookDao.findById(book.getBookId());
            if (!existingBookOpt.isPresent()) {
                return BookManagementResult.failure("图书不存在: " + book.getBookId());
            }
            
            // 3. 数据验证
            BookManagementResult validationResult = validateBook(book);
            if (!validationResult.isSuccess()) {
                return validationResult;
            }
            
            // 4. 检查ISBN是否已被其他图书使用
            if (isIsbnExists(book.getIsbn(), book.getBookId())) {
                return BookManagementResult.failure("ISBN已被其他图书使用: " + book.getIsbn());
            }
            
            // 5. 设置更新时间
            book.setUpdateTime(LocalDateTime.now());
            
            // 6. 更新图书
            Book updatedBook = bookDao.save(book);
            
            log.info("图书更新成功: bookId={}, title={}", updatedBook.getBookId(), updatedBook.getTitle());
            return BookManagementResult.success("图书更新成功", updatedBook);
            
        } catch (Exception e) {
            log.error("更新图书异常: bookId={}, title={}", book.getBookId(), book.getTitle(), e);
            return BookManagementResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    @Override
    public BookManagementResult deleteBook(Integer bookId) {
        log.info("开始删除图书: bookId={}", bookId);
        
        try {
            // 1. 参数验证
            if (bookId == null) {
                return BookManagementResult.failure("图书ID不能为空");
            }
            
            // 2. 检查图书是否存在
            Optional<Book> bookOpt = bookDao.findById(bookId);
            if (!bookOpt.isPresent()) {
                return BookManagementResult.failure("图书不存在: " + bookId);
            }
            
            // 3. 检查是否可以删除
            if (!canDeleteBook(bookId)) {
                return BookManagementResult.failure("图书正在被借阅，无法删除");
            }
            
            // 4. 删除图书
            bookDao.deleteById(bookId);
            
            log.info("图书删除成功: bookId={}", bookId);
            return BookManagementResult.success("图书删除成功", bookId);
            
        } catch (Exception e) {
            log.error("删除图书异常: bookId={}", bookId, e);
            return BookManagementResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    @Override
    public Book getBookById(Integer bookId) {
        log.info("查询图书: bookId={}", bookId);
        
        try {
            if (bookId == null) {
                log.warn("图书ID为空");
                return null;
            }
            
            Optional<Book> bookOpt = bookDao.findById(bookId);
            if (bookOpt.isPresent()) {
                log.info("查询图书成功: bookId={}, title={}", bookId, bookOpt.get().getTitle());
                return bookOpt.get();
            } else {
                log.warn("图书不存在: bookId={}", bookId);
                return null;
            }
            
        } catch (Exception e) {
            log.error("查询图书异常: bookId={}", bookId, e);
            return null;
        }
    }
    
    @Override
    public Book getBookByIsbn(String isbn) {
        log.info("根据ISBN查询图书: isbn={}", isbn);
        
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                log.warn("ISBN为空");
                return null;
            }
            
            Optional<Book> bookOpt = bookDao.findByIsbn(isbn.trim());
            if (bookOpt.isPresent()) {
                log.info("根据ISBN查询图书成功: isbn={}, title={}", isbn, bookOpt.get().getTitle());
                return bookOpt.get();
            } else {
                log.warn("图书不存在: isbn={}", isbn);
                return null;
            }
            
        } catch (Exception e) {
            log.error("根据ISBN查询图书异常: isbn={}", isbn, e);
            return null;
        }
    }
    
    // ==================== 查询功能 ====================
    
    @Override
    public List<Book> getAllBooks() {
        log.info("查询所有图书");
        
        try {
            List<Book> books = bookDao.findAll();
            log.info("查询所有图书成功: count={}", books.size());
            return books;
            
        } catch (Exception e) {
            log.error("查询所有图书异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Book> getBooksByPage(int page, int size) {
        log.info("分页查询图书: page={}, size={}", page, size);
        
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            
            int offset = (page - 1) * size;
            List<Book> books = bookDao.findByPage(offset, size);
            
            log.info("分页查询图书成功: page={}, size={}, count={}", page, size, books.size());
            return books;
            
        } catch (Exception e) {
            log.error("分页查询图书异常: page={}, size={}", page, size, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Book> searchBooksByTitle(String title) {
        log.info("根据书名搜索图书: title={}", title);
        
        try {
            if (title == null || title.trim().isEmpty()) {
                log.warn("书名为空");
                return new ArrayList<>();
            }
            
            List<Book> books = bookDao.findByTitleLike(title.trim());
            log.info("根据书名搜索图书成功: title={}, count={}", title, books.size());
            return books;
            
        } catch (Exception e) {
            log.error("根据书名搜索图书异常: title={}", title, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Book> searchBooksByAuthor(String author) {
        log.info("根据作者搜索图书: author={}", author);
        
        try {
            if (author == null || author.trim().isEmpty()) {
                log.warn("作者为空");
                return new ArrayList<>();
            }
            
            List<Book> books = bookDao.findByAuthorLike(author.trim());
            log.info("根据作者搜索图书成功: author={}, count={}", author, books.size());
            return books;
            
        } catch (Exception e) {
            log.error("根据作者搜索图书异常: author={}", author, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Book> searchBooksByPublisher(String publisher) {
        log.info("根据出版社搜索图书: publisher={}", publisher);
        
        try {
            if (publisher == null || publisher.trim().isEmpty()) {
                log.warn("出版社为空");
                return new ArrayList<>();
            }
            
            List<Book> books = bookDao.findByPublisherLike(publisher.trim());
            log.info("根据出版社搜索图书成功: publisher={}, count={}", publisher, books.size());
            return books;
            
        } catch (Exception e) {
            log.error("根据出版社搜索图书异常: publisher={}", publisher, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Book> getBooksByCategory(String category) {
        log.info("根据分类查询图书: category={}", category);
        
        try {
            if (category == null || category.trim().isEmpty()) {
                log.warn("分类为空");
                return new ArrayList<>();
            }
            
            List<Book> books = bookDao.findByCategory(category.trim());
            log.info("根据分类查询图书成功: category={}, count={}", category, books.size());
            return books;
            
        } catch (Exception e) {
            log.error("根据分类查询图书异常: category={}", category, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Book> getBooksByStatus(BookStatus status) {
        log.info("根据状态查询图书: status={}", status);
        
        try {
            if (status == null) {
                log.warn("状态为空");
                return new ArrayList<>();
            }
            
            List<Book> books = bookDao.findByStatus(status);
            log.info("根据状态查询图书成功: status={}, count={}", status, books.size());
            return books;
            
        } catch (Exception e) {
            log.error("根据状态查询图书异常: status={}", status, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Book> searchBooks(String title, String author, String publisher, String category, BookStatus status) {
        log.info("复合查询图书: title={}, author={}, publisher={}, category={}, status={}", 
                title, author, publisher, category, status);
        
        try {
            List<Book> books = bookDao.findByConditions(title, author, publisher, category, status);
            log.info("复合查询图书成功: count={}", books.size());
            return books;
            
        } catch (Exception e) {
            log.error("复合查询图书异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Book> smartSearchBooks(String keyword) {
        log.info("智能搜索图书: keyword={}", keyword);
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("搜索关键词为空");
                return new ArrayList<>();
            }
            
            String trimmedKeyword = keyword.trim();
            List<Book> books = new ArrayList<>();
            
            // 1. 判断是否为ISBN格式（精确搜索）
            if (isISBN(trimmedKeyword)) {
                log.info("检测到ISBN格式，进行精确搜索");
                Optional<Book> bookOpt = bookDao.findByIsbn(trimmedKeyword);
                if (bookOpt.isPresent()) {
                    books.add(bookOpt.get());
                }
            }
            // 2. 判断是否为分类（精确搜索）
            else if (isCategory(trimmedKeyword)) {
                log.info("检测到分类格式，进行精确搜索");
                books = bookDao.findByCategory(trimmedKeyword);
            }
            // 3. 否则进行模糊搜索（书名、作者、出版社）
            else {
                log.info("进行模糊搜索: {}", trimmedKeyword);
                books = bookDao.searchBooks(trimmedKeyword);
            }
            
            log.info("智能搜索图书成功: count={}", books.size());
            return books;
            
        } catch (Exception e) {
            log.error("智能搜索图书异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<PopularBook> getPopularBooks(int limit) {
        log.info("获取热门图书: limit={}", limit);
        
        try {
            if (limit <= 0) {
                log.warn("限制数量无效: {}", limit);
                return new ArrayList<>();
            }
            
            List<PopularBook> popularBooks = bookDao.getPopularBooks();
            if (popularBooks.size() > limit) {
                popularBooks = popularBooks.subList(0, limit);
            }
            
            log.info("获取热门图书成功: count={}", popularBooks.size());
            return popularBooks;
            
        } catch (Exception e) {
            log.error("获取热门图书异常", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 判断是否为ISBN格式
     */
    private boolean isISBN(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        // ISBN格式：13位数字或10位数字，可能包含连字符和空格
        String cleanKeyword = keyword.replaceAll("[\\s-]", "");
        return cleanKeyword.matches("\\d{10}|\\d{13}");
    }
    
    /**
     * 判断是否为分类
     */
    private boolean isCategory(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        String[] categories = {"文学", "科技", "历史", "艺术", "教育", "其他"};
        for (String category : categories) {
            if (category.equals(keyword.trim())) {
                return true;
            }
        }
        return false;
    }
    
    // ==================== 批量操作 ====================
    
    @Override
    public BookManagementResult batchAddBooks(List<Book> books) {
        log.info("批量添加图书: count={}", books.size());
        
        try {
            if (books == null || books.isEmpty()) {
                return BookManagementResult.failure("图书列表不能为空");
            }
            
            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (Book book : books) {
                try {
                    BookManagementResult result = addBook(book);
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                        errors.add("图书 " + book.getTitle() + ": " + result.getMessage());
                    }
                } catch (Exception e) {
                    failCount++;
                    errors.add("图书 " + book.getTitle() + ": " + e.getMessage());
                }
            }
            
            String message = String.format("批量添加完成，成功: %d, 失败: %d", successCount, failCount);
            log.info("批量添加图书完成: {}", message);
            
            if (failCount == 0) {
                return BookManagementResult.success(message, successCount, failCount);
            } else {
                return BookManagementResult.failure(message, successCount, failCount, errors);
            }
            
        } catch (Exception e) {
            log.error("批量添加图书异常", e);
            return BookManagementResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    @Override
    public BookManagementResult batchUpdateBooks(List<Book> books) {
        log.info("批量更新图书: count={}", books.size());
        
        try {
            if (books == null || books.isEmpty()) {
                return BookManagementResult.failure("图书列表不能为空");
            }
            
            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (Book book : books) {
                try {
                    BookManagementResult result = updateBook(book);
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                        errors.add("图书 " + book.getTitle() + ": " + result.getMessage());
                    }
                } catch (Exception e) {
                    failCount++;
                    errors.add("图书 " + book.getTitle() + ": " + e.getMessage());
                }
            }
            
            String message = String.format("批量更新完成，成功: %d, 失败: %d", successCount, failCount);
            log.info("批量更新图书完成: {}", message);
            
            if (failCount == 0) {
                return BookManagementResult.success(message, successCount, failCount);
            } else {
                return BookManagementResult.failure(message, successCount, failCount, errors);
            }
            
        } catch (Exception e) {
            log.error("批量更新图书异常", e);
            return BookManagementResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    @Override
    public BookManagementResult batchDeleteBooks(List<Integer> bookIds) {
        log.info("批量删除图书: count={}", bookIds.size());
        
        try {
            if (bookIds == null || bookIds.isEmpty()) {
                return BookManagementResult.failure("图书ID列表不能为空");
            }
            
            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (Integer bookId : bookIds) {
                try {
                    BookManagementResult result = deleteBook(bookId);
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                        errors.add("图书ID " + bookId + ": " + result.getMessage());
                    }
                } catch (Exception e) {
                    failCount++;
                    errors.add("图书ID " + bookId + ": " + e.getMessage());
                }
            }
            
            String message = String.format("批量删除完成，成功: %d, 失败: %d", successCount, failCount);
            log.info("批量删除图书完成: {}", message);
            
            if (failCount == 0) {
                return BookManagementResult.success(message, successCount, failCount);
            } else {
                return BookManagementResult.failure(message, successCount, failCount, errors);
            }
            
        } catch (Exception e) {
            log.error("批量删除图书异常", e);
            return BookManagementResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    @Override
    public BookManagementResult batchUpdateBookStatus(List<Integer> bookIds, BookStatus status) {
        log.info("批量更新图书状态: count={}, status={}", bookIds.size(), status);
        
        try {
            if (bookIds == null || bookIds.isEmpty()) {
                return BookManagementResult.failure("图书ID列表不能为空");
            }
            if (status == null) {
                return BookManagementResult.failure("状态不能为空");
            }
            
            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (Integer bookId : bookIds) {
                try {
                    Book book = getBookById(bookId);
                    if (book != null) {
                        book.setStatus(status);
                        book.setUpdateTime(LocalDateTime.now());
                        
                        BookManagementResult result = updateBook(book);
                        if (result.isSuccess()) {
                            successCount++;
                        } else {
                            failCount++;
                            errors.add("图书ID " + bookId + ": " + result.getMessage());
                        }
                    } else {
                        failCount++;
                        errors.add("图书ID " + bookId + ": 图书不存在");
                    }
                } catch (Exception e) {
                    failCount++;
                    errors.add("图书ID " + bookId + ": " + e.getMessage());
                }
            }
            
            String message = String.format("批量更新状态完成，成功: %d, 失败: %d", successCount, failCount);
            log.info("批量更新图书状态完成: {}", message);
            
            if (failCount == 0) {
                return BookManagementResult.success(message, successCount, failCount);
            } else {
                return BookManagementResult.failure(message, successCount, failCount, errors);
            }
            
        } catch (Exception e) {
            log.error("批量更新图书状态异常", e);
            return BookManagementResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== Excel导入导出 ====================
    
    @Override
    public ExcelImportResult importBooksFromExcel(InputStream inputStream, String fileName) {
        log.info("开始从Excel导入图书: fileName={}", fileName);
        
        try {
            // 1. 解析Excel文件
            List<Book> books = parseExcelToBooks(inputStream, fileName);
            
            if (books.isEmpty()) {
                return ExcelImportResult.failure("Excel文件中没有有效的图书数据");
            }
            
            // 2. 调用批量添加功能（对接点）
            BookManagementResult batchResult = batchAddBooks(books);
            
            // 3. 转换结果格式
            int totalCount = books.size();
            int successCount = batchResult.getSuccessCount() != null ? batchResult.getSuccessCount() : 0;
            int failCount = batchResult.getFailCount() != null ? batchResult.getFailCount() : 0;
            
            if (batchResult.isSuccess()) {
                log.info("Excel导入成功: fileName={}, 成功: {}, 失败: {}", 
                        fileName, successCount, failCount);
                return ExcelImportResult.success(
                    "Excel导入完成，成功: " + successCount + ", 失败: " + failCount,
                    totalCount,
                    successCount,
                    failCount,
                    0 // skipCount
                );
            } else {
                log.warn("Excel导入失败: fileName={}, reason={}", fileName, batchResult.getMessage());
                return ExcelImportResult.partialSuccess(
                    batchResult.getMessage(),
                    totalCount,
                    successCount,
                    failCount,
                    0, // skipCount
                    batchResult.getErrors(),
                    null // warnings
                );
            }
            
        } catch (Exception e) {
            log.error("从Excel导入图书异常: fileName={}", fileName, e);
            return ExcelImportResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 解析Excel文件为图书列表
     * 这是Excel导入的核心解析逻辑
     */
    private List<Book> parseExcelToBooks(InputStream inputStream, String fileName) throws Exception {
        List<Book> books = new ArrayList<>();
        
        try {
            // 根据文件扩展名选择工作簿类型
            Workbook workbook;
            if (fileName.toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else if (fileName.toLowerCase().endsWith(".xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                throw new IllegalArgumentException("不支持的文件格式，请使用.xlsx或.xls文件");
            }
            
            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Excel文件中没有工作表");
            }
            
            // 解析数据行（跳过标题行）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Book book = parseRowToBook(row, i + 1);
                    if (book != null) {
                        books.add(book);
                    }
                } catch (Exception e) {
                    log.warn("解析第{}行数据失败: {}", i + 1, e.getMessage());
                    // 继续处理下一行，不中断整个导入过程
                }
            }
            
            workbook.close();
            
        } catch (Exception e) {
            log.error("解析Excel文件异常: fileName={}", fileName, e);
            throw new Exception("解析Excel文件失败: " + e.getMessage());
        }
        
        return books;
    }
    
    /**
     * 将Excel行数据解析为图书对象
     */
    private Book parseRowToBook(Row row, int rowNumber) throws Exception {
        try {
            // Excel列定义：
            // A列：书名, B列：作者, C列：ISBN, D列：出版社, E列：出版日期, F列：分类, G列：位置, H列：总数量
            
            String title = getCellStringValue(row.getCell(0));
            String author = getCellStringValue(row.getCell(1));
            String isbn = getCellStringValue(row.getCell(2));
            String publisher = getCellStringValue(row.getCell(3));
            String publishDateStr = getCellStringValue(row.getCell(4));
            String category = getCellStringValue(row.getCell(5));
            String location = getCellStringValue(row.getCell(6));
            Integer totalQty = getCellIntegerValue(row.getCell(7));
            
            // 基本字段验证
            if (title == null || title.trim().isEmpty()) {
                throw new Exception("第" + rowNumber + "行：书名不能为空");
            }
            if (author == null || author.trim().isEmpty()) {
                throw new Exception("第" + rowNumber + "行：作者不能为空");
            }
            if (isbn == null || isbn.trim().isEmpty()) {
                throw new Exception("第" + rowNumber + "行：ISBN不能为空");
            }
            if (publisher == null || publisher.trim().isEmpty()) {
                throw new Exception("第" + rowNumber + "行：出版社不能为空");
            }
            
            // 构建图书对象
            Book book = Book.builder()
                    .title(title.trim())
                    .author(author.trim())
                    .isbn(isbn.trim())
                    .publisher(publisher.trim())
                    .category(category != null ? category.trim() : null)
                    .location(location != null ? location.trim() : null)
                    .totalQty(totalQty != null ? totalQty : 1)
                    .availQty(totalQty != null ? totalQty : 1)
                    .status(BookStatus.IN_LIBRARY)
                    .build();
            
            // 解析出版日期
            if (publishDateStr != null && !publishDateStr.trim().isEmpty()) {
                try {
                    // 支持多种日期格式
                    LocalDate publishDate = parseDate(publishDateStr.trim());
                    book.setPublishDate(publishDate);
                } catch (Exception e) {
                    log.warn("第{}行：出版日期格式错误，忽略: {}", rowNumber, publishDateStr);
                }
            }
            
            return book;
            
        } catch (Exception e) {
            log.error("解析第{}行数据异常: {}", rowNumber, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
                         case NUMERIC:
                 // 检查是否为日期格式
                 if (cell.getCellStyle().getDataFormatString().contains("m") || 
                     cell.getCellStyle().getDataFormatString().contains("d") ||
                     cell.getCellStyle().getDataFormatString().contains("y")) {
                     return cell.getDateCellValue().toString();
                 } else {
                    // 数字转字符串，避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    /**
     * 获取单元格整数值
     */
    private Integer getCellIntegerValue(Cell cell) {
        if (cell == null) return null;
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case STRING:
                    String stringValue = cell.getStringCellValue().trim();
                    if (stringValue.isEmpty()) return null;
                    return Integer.parseInt(stringValue);
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("解析单元格整数值失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析日期字符串
     */
    private LocalDate parseDate(String dateStr) throws Exception {
        // 支持多种日期格式
        String[] patterns = {
            "yyyy-MM-dd",
            "yyyy/MM/dd", 
            "yyyy年MM月dd日",
            "MM/dd/yyyy",
            "dd/MM/yyyy"
        };
        
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception ignored) {
                // 继续尝试下一个格式
            }
        }
        
        throw new Exception("不支持的日期格式: " + dateStr);
    }
    
    @Override
    public BookManagementResult exportBooksToExcel(List<Book> books, String fileName) {
        log.info("开始导出图书到Excel: fileName={}, count={}", fileName, books.size());
        
        try {
            // TODO: 实现Excel导出逻辑
            // 这里需要添加Apache POI依赖来处理Excel文件
            // 暂时返回一个模拟结果
            
            log.warn("Excel导出功能暂未实现，需要添加Apache POI依赖");
            return BookManagementResult.failure("Excel导出功能暂未实现，需要添加Apache POI依赖");
            
        } catch (Exception e) {
            log.error("导出图书到Excel异常: fileName={}", fileName, e);
            return BookManagementResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 统计功能 ====================
    
    @Override
    public long countAllBooks() {
        log.info("统计图书总数");
        
        try {
            long count = bookDao.count();
            log.info("统计图书总数成功: count={}", count);
            return count;
            
        } catch (Exception e) {
            log.error("统计图书总数异常", e);
            return 0;
        }
    }
    
    @Override
    public Map<BookStatus, Long> countBooksByStatus() {
        log.info("统计各状态图书数量");
        
        try {
            Map<BookStatus, Long> statistics = new HashMap<>();
            
            for (BookStatus status : BookStatus.values()) {
                long count = bookDao.countByStatus(status);
                statistics.put(status, count);
            }
            
            log.info("统计各状态图书数量成功: {}", statistics);
            return statistics;
            
        } catch (Exception e) {
            log.error("统计各状态图书数量异常", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Long> countBooksByCategory() {
        log.info("统计各分类图书数量");
        
        try {
            Map<String, Long> statistics = bookDao.countByCategory();
            log.info("统计各分类图书数量成功: {}", statistics);
            return statistics;
            
        } catch (Exception e) {
            log.error("统计各分类图书数量异常", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public long countAvailableBooks() {
        log.info("统计可借阅图书数量");
        
        try {
            long count = bookDao.countByStatus(BookStatus.AVAILABLE);
            log.info("统计可借阅图书数量成功: count={}", count);
            return count;
            
        } catch (Exception e) {
            log.error("统计可借阅图书数量异常", e);
            return 0;
        }
    }
    
    @Override
    public long countBorrowedBooks() {
        log.info("统计借出图书数量");
        
        try {
            long count = bookDao.countByStatus(BookStatus.BORROWED);
            log.info("统计借出图书数量成功: count={}", count);
            return count;
            
        } catch (Exception e) {
            log.error("统计借出图书数量异常", e);
            return 0;
        }
    }
    
    // ==================== 库存管理 ====================
    
    @Override
    public BookManagementResult updateBookCopies(Integer bookId, Integer totalCopies, Integer availableCopies) {
        log.info("更新图书副本数量: bookId={}, totalCopies={}, availableCopies={}", 
                bookId, totalCopies, availableCopies);
        
        try {
            if (bookId == null) {
                return BookManagementResult.failure("图书ID不能为空");
            }
            if (totalCopies == null || totalCopies < 0) {
                return BookManagementResult.failure("总副本数不能为空且必须大于等于0");
            }
            if (availableCopies == null || availableCopies < 0) {
                return BookManagementResult.failure("可借副本数不能为空且必须大于等于0");
            }
            if (availableCopies > totalCopies) {
                return BookManagementResult.failure("可借副本数不能大于总副本数");
            }
            
            Book book = getBookById(bookId);
            if (book == null) {
                return BookManagementResult.failure("图书不存在: " + bookId);
            }
            
            book.setTotalQty(totalCopies);
            book.setAvailQty(availableCopies);
            book.setUpdateTime(LocalDateTime.now());
            
            BookManagementResult result = updateBook(book);
            if (result.isSuccess()) {
                log.info("更新图书副本数量成功: bookId={}", bookId);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("更新图书副本数量异常: bookId={}", bookId, e);
            return BookManagementResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    @Override
    public BookManagementResult reduceBookCopies(Integer bookId, Integer copies) {
        log.info("减少图书副本: bookId={}, copies={}", bookId, copies);
        
        try {
            if (bookId == null) {
                return BookManagementResult.failure("图书ID不能为空");
            }
            if (copies == null || copies <= 0) {
                return BookManagementResult.failure("减少的副本数必须大于0");
            }
            
            Book book = getBookById(bookId);
            if (book == null) {
                return BookManagementResult.failure("图书不存在: " + bookId);
            }
            
            int currentTotal = book.getTotalQty() != null ? book.getTotalQty() : 0;
            int currentAvailable = book.getAvailQty() != null ? book.getAvailQty() : 0;
            
            if (copies > currentTotal) {
                return BookManagementResult.failure("减少的副本数不能大于总副本数");
            }
            
            int newTotalCopies = currentTotal - copies;
            int newAvailableCopies = Math.max(0, currentAvailable - copies);
            
            return updateBookCopies(bookId, newTotalCopies, newAvailableCopies);
            
        } catch (Exception e) {
            log.error("减少图书副本异常: bookId={}, copies={}", bookId, copies, e);
            return BookManagementResult.failure("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 数据验证 ====================
    
    @Override
    public BookManagementResult validateBook(Book book) {
        if (book == null) {
            return BookManagementResult.failure("图书信息不能为空");
        }
        
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            return BookManagementResult.failure("书名不能为空");
        }
        
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            return BookManagementResult.failure("作者不能为空");
        }
        
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            return BookManagementResult.failure("ISBN不能为空");
        }
        
        if (book.getPublisher() == null || book.getPublisher().trim().isEmpty()) {
            return BookManagementResult.failure("出版社不能为空");
        }
        
        if (book.getTotalQty() != null && book.getTotalQty() < 0) {
            return BookManagementResult.failure("总副本数不能小于0");
        }
        
        if (book.getAvailQty() != null && book.getAvailQty() < 0) {
            return BookManagementResult.failure("可借副本数不能小于0");
        }
        
        if (book.getTotalQty() != null && book.getAvailQty() != null && 
            book.getAvailQty() > book.getTotalQty()) {
            return BookManagementResult.failure("可借副本数不能大于总副本数");
        }
        
        return BookManagementResult.success("验证通过", book);
    }
    
    @Override
    public boolean isIsbnExists(String isbn, Integer excludeBookId) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                return false;
            }
            
            Optional<Book> bookOpt = bookDao.findByIsbn(isbn.trim());
            if (bookOpt.isPresent()) {
                // 如果指定了排除的图书ID，则检查是否是同一本书
                if (excludeBookId != null && bookOpt.get().getBookId().equals(excludeBookId)) {
                    return false; // 同一本书，不算重复
                }
                return true; // 其他图书使用了这个ISBN
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("检查ISBN是否存在异常: isbn={}", isbn, e);
            return false;
        }
    }
    
    @Override
    public boolean canDeleteBook(Integer bookId) {
        try {
            if (bookId == null) {
                return false;
            }
            
            Book book = getBookById(bookId);
            if (book == null) {
                return false;
            }
            
            // 检查是否有可借副本（即是否有图书被借出）
            if (book.getAvailQty() != null && book.getTotalQty() != null) {
                return book.getAvailQty().equals(book.getTotalQty());
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("检查图书是否可以删除异常: bookId={}", bookId, e);
            return false;
        }
    }
    
    // ==================== 借阅记录管理 ====================
    
    @Override
    public List<Map<String, Object>> getAllBorrowRecords() {
        log.info("获取所有借阅记录");
        
        try {
            // 调用借阅记录DAO获取真实数据
            com.vcampus.server.core.library.dao.BookBorrowDao borrowDao = 
                com.vcampus.server.core.library.dao.BookBorrowDao.getInstance();
            List<com.vcampus.server.core.library.entity.view.UserBorrowHistory> borrowHistoryList = 
                borrowDao.getAllUserBorrowHistory();
            
            // 转换为前端需要的格式
            List<Map<String, Object>> records = new ArrayList<>();
            for (com.vcampus.server.core.library.entity.view.UserBorrowHistory history : borrowHistoryList) {
                Map<String, Object> record = new HashMap<>();
                record.put("transId", history.getTransId());
                record.put("userId", history.getCardNum());
                record.put("userName", history.getUserName());
                record.put("bookTitle", history.getBookTitle());
                record.put("borrowDate", history.getBorrowTime() != null ? 
                    history.getBorrowTime().toLocalDate().toString() : "");
                record.put("dueDate", history.getDueTime() != null ? 
                    history.getDueTime().toLocalDate().toString() : "");
                record.put("returnDate", history.getReturnTime() != null ? 
                    history.getReturnTime().toLocalDate().toString() : null); // 归还日期，未归还是null
                record.put("status", formatBorrowStatus(history.getBorrowStatus()));
                record.put("remark", ""); // 备注字段暂时为空
                records.add(record);
            }
            
            log.info("获取借阅记录成功: count={}", records.size());
            return records;
            
        } catch (Exception e) {
            log.error("获取借阅记录异常", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 格式化借阅状态显示
     */
    private String formatBorrowStatus(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "BORROWED": return "已借出";
            case "RETURNED": return "已归还";
            case "OVERDUE": return "逾期";
            default: return status;
        }
    }
    
    /**
     * 将后端状态转换为前端显示状态
     */
    private String convertBackendToFrontendStatus(String backendStatus) {
        if (backendStatus == null) return "未知";
        switch (backendStatus) {
            case "RETURNED": return "已归还";
            case "OVERDUE": return "逾期";
            case "RENEWED": return "续借";
            case "BORROWED": return "已借出";
            default: return backendStatus;
        }
    }
    
    @Override
    public List<Map<String, Object>> searchBorrowRecords(String userSearch, String bookSearch, String status, String borrowDate, String dueDate, String returnDate) {
        log.info("搜索借阅记录: userSearch={}, bookSearch={}, status={}, borrowDate={}, dueDate={}, returnDate={}", userSearch, bookSearch, status, borrowDate, dueDate, returnDate);
        
        try {
            // 获取所有借阅记录
            List<Map<String, Object>> allRecords = getAllBorrowRecords();
            List<Map<String, Object>> filteredRecords = new ArrayList<>();
            
            // 根据搜索条件过滤
            for (Map<String, Object> record : allRecords) {
                boolean matches = true;
                
                // 用户搜索
                if (userSearch != null && !userSearch.trim().isEmpty()) {
                    String userId = (String) record.get("userId");
                    String userName = (String) record.get("userName");
                    String searchTerm = userSearch.toLowerCase();
                    boolean userMatches = (userId != null && userId.toLowerCase().contains(searchTerm)) ||
                                       (userName != null && userName.toLowerCase().contains(searchTerm));
                    if (!userMatches) {
                        matches = false;
                    }
                }
                
                // 图书搜索
                if (bookSearch != null && !bookSearch.trim().isEmpty()) {
                    String bookTitle = (String) record.get("bookTitle");
                    if (bookTitle == null || !bookTitle.toLowerCase().contains(bookSearch.toLowerCase())) {
                        matches = false;
                    }
                }
                
                // 状态搜索
                if (status != null && !status.trim().isEmpty() && !"全部".equals(status)) {
                    String recordStatus = (String) record.get("status");
                    // 将后端状态转换为前端状态进行比较
                    String frontendStatus = convertBackendToFrontendStatus(status);
                    if (recordStatus == null || !recordStatus.equals(frontendStatus)) {
                        matches = false;
                    }
                }
                
                // 借阅日期搜索
                if (borrowDate != null && !borrowDate.trim().isEmpty()) {
                    String recordBorrowDate = (String) record.get("borrowDate");
                    if (recordBorrowDate == null || !recordBorrowDate.startsWith(borrowDate)) {
                        matches = false;
                    }
                }
                
                // 应还日期搜索
                if (dueDate != null && !dueDate.trim().isEmpty()) {
                    String recordDueDate = (String) record.get("dueDate");
                    if (recordDueDate == null || !recordDueDate.startsWith(dueDate)) {
                        matches = false;
                    }
                }
                
                // 归还日期搜索
                if (returnDate != null && !returnDate.trim().isEmpty()) {
                    String recordReturnDate = (String) record.get("returnDate");
                    if (recordReturnDate == null || !recordReturnDate.startsWith(returnDate)) {
                        matches = false;
                    }
                }
                
                if (matches) {
                    filteredRecords.add(record);
                }
            }
            
            log.info("搜索借阅记录成功: count={}", filteredRecords.size());
            return filteredRecords;
            
        } catch (Exception e) {
            log.error("搜索借阅记录异常: userSearch={}, bookSearch={}, status={}", userSearch, bookSearch, status, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public BookManagementResult forceReturnBook(Integer transId) {
        log.info("强制归还图书: transId={}", transId);
        
        try {
            // 调用借阅记录DAO的归还方法
            com.vcampus.server.core.library.dao.BookBorrowDao borrowDao = 
                com.vcampus.server.core.library.dao.BookBorrowDao.getInstance();
            
            // 检查借阅记录是否存在
            if (!borrowDao.existsById(transId)) {
                return BookManagementResult.builder()
                    .success(false)
                    .message("借阅记录不存在")
                    .build();
            }
            
            // 调用存储过程归还图书
            com.vcampus.server.core.library.entity.result.ReturnResult returnResult = 
                borrowDao.returnBook(transId);
            
            if (returnResult.isSuccess()) {
                log.info("强制归还图书成功: transId={}", transId);
                return BookManagementResult.builder()
                    .success(true)
                    .message("强制归还成功")
                    .build();
            } else {
                log.warn("强制归还图书失败: transId={}, message={}", transId, returnResult.getMessage());
                return BookManagementResult.builder()
                    .success(false)
                    .message("强制归还失败: " + returnResult.getMessage())
                    .build();
            }
            
        } catch (Exception e) {
            log.error("强制归还图书异常: transId={}", transId, e);
            return BookManagementResult.builder()
                .success(false)
                .message("系统错误: " + e.getMessage())
                .build();
        }
    }
    
    @Override
    public BookManagementResult adminRenewBook(Integer transId, Integer extendDays) {
        log.info("管理员续借图书: transId={}, extendDays={}", transId, extendDays);
        
        try {
            // 调用借阅记录DAO的续借方法
            com.vcampus.server.core.library.dao.BookBorrowDao borrowDao = 
                com.vcampus.server.core.library.dao.BookBorrowDao.getInstance();
            
            // 检查借阅记录是否存在
            if (!borrowDao.existsById(transId)) {
                return BookManagementResult.builder()
                    .success(false)
                    .message("借阅记录不存在")
                    .build();
            }
            
            // 调用存储过程续借图书
            com.vcampus.server.core.library.entity.result.RenewResult renewResult = 
                borrowDao.renewBook(transId, extendDays);
            
            if (renewResult.isSuccess()) {
                log.info("管理员续借图书成功: transId={}, extendDays={}", transId, extendDays);
                return BookManagementResult.builder()
                    .success(true)
                    .message("续借成功，延长" + extendDays + "天")
                    .build();
            } else {
                log.warn("管理员续借图书失败: transId={}, message={}", transId, renewResult.getMessage());
                return BookManagementResult.builder()
                    .success(false)
                    .message("续借失败: " + renewResult.getMessage())
                    .build();
            }
            
        } catch (Exception e) {
            log.error("管理员续借图书异常: transId={}, extendDays={}", transId, extendDays, e);
            return BookManagementResult.builder()
                .success(false)
                .message("系统错误: " + e.getMessage())
                .build();
        }
    }
    
    @Override
    public BookManagementResult handleOverdueBook(Integer transId) {
        log.info("处理逾期图书: transId={}", transId);
        
        try {
            // 调用借阅记录DAO更新状态为逾期
            com.vcampus.server.core.library.dao.BookBorrowDao borrowDao = 
                com.vcampus.server.core.library.dao.BookBorrowDao.getInstance();
            
            // 检查借阅记录是否存在
            if (!borrowDao.existsById(transId)) {
                return BookManagementResult.builder()
                    .success(false)
                    .message("借阅记录不存在")
                    .build();
            }
            
            // 更新状态为逾期
            borrowDao.updateStatus(transId, com.vcampus.server.core.library.enums.BorrowStatus.OVERDUE);
            
            log.info("处理逾期图书成功: transId={}", transId);
            return BookManagementResult.builder()
                .success(true)
                .message("逾期处理成功")
                .build();
            
        } catch (Exception e) {
            log.error("处理逾期图书异常: transId={}", transId, e);
            return BookManagementResult.builder()
                .success(false)
                .message("系统错误: " + e.getMessage())
                .build();
        }
    }
    
    @Override
    public List<Map<String, Object>> getOverdueBorrowRecords() {
        log.info("获取逾期借阅记录");
        
        try {
            // 调用借阅记录DAO获取逾期记录
            com.vcampus.server.core.library.dao.BookBorrowDao borrowDao = 
                com.vcampus.server.core.library.dao.BookBorrowDao.getInstance();
            List<com.vcampus.server.core.library.entity.view.OverdueDetails> overdueList = 
                borrowDao.getOverdueDetails();
            
            // 转换为前端需要的格式
            List<Map<String, Object>> records = new ArrayList<>();
            for (com.vcampus.server.core.library.entity.view.OverdueDetails overdue : overdueList) {
                Map<String, Object> record = new HashMap<>();
                record.put("transId", overdue.getTransId());
                record.put("userId", overdue.getCardNum());
                record.put("userName", overdue.getUserName());
                record.put("bookTitle", overdue.getBookTitle());
                record.put("borrowDate", overdue.getBorrowTime() != null ? 
                    overdue.getBorrowTime().toLocalDate().toString() : "");
                record.put("dueDate", overdue.getDueTime() != null ? 
                    overdue.getDueTime().toLocalDate().toString() : "");
                record.put("overdueDays", overdue.getOverdueDays());
                record.put("status", "逾期");
                record.put("remark", ""); // 备注字段暂时为空
                records.add(record);
            }
            
            log.info("获取逾期借阅记录成功: count={}", records.size());
            return records;
            
        } catch (Exception e) {
            log.error("获取逾期借阅记录异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public BookManagementResult addBookCopies(Integer bookId, Integer copies) {
        log.info("增加图书副本: bookId={}, copies={}", bookId, copies);
        
        try {
            if (bookId == null || copies == null || copies <= 0) {
                return BookManagementResult.builder()
                    .success(false)
                    .message("参数错误：图书ID和副本数不能为空，副本数必须大于0")
                    .build();
            }
            
            Book book = getBookById(bookId);
            if (book == null) {
                return BookManagementResult.builder()
                    .success(false)
                    .message("图书不存在")
                    .build();
            }
            
            // 更新总副本数和可借副本数
            Integer newTotalQty = book.getTotalQty() + copies;
            Integer newAvailQty = book.getAvailQty() + copies;
            
            // 更新数据库
            bookDao.updateStock(bookId, newAvailQty);
            
            // 更新图书信息
            bookDao.updateBook(bookId, book.getTitle(), book.getAuthor(), book.getPublisher(), 
                book.getPublishDate(), book.getCategory(), book.getLocation(), newTotalQty);
            
            log.info("增加图书副本成功: bookId={}, copies={}, newTotalQty={}, newAvailQty={}", 
                bookId, copies, newTotalQty, newAvailQty);
            
            return BookManagementResult.builder()
                .success(true)
                .message("增加副本成功")
                .build();
                
        } catch (Exception e) {
            log.error("增加图书副本异常: bookId={}, copies={}", bookId, copies, e);
            return BookManagementResult.builder()
                .success(false)
                .message("系统错误: " + e.getMessage())
                .build();
        }
    }
    
    @Override
    public List<Map<String, Object>> getUserBorrowRanking() {
        try {
            log.info("获取用户借阅排名");
            
            // 调用DAO层获取用户借阅统计数据
            List<Map<String, Object>> rankingData = bookBorrowDao.getUserBorrowRanking();
            
            log.info("获取用户借阅排名成功: count={}", rankingData.size());
            return rankingData;
            
        } catch (Exception e) {
            log.error("获取用户借阅排名异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getAllCategories() {
        log.info("获取所有分类列表");
        
        try {
            // 从数据库获取所有分类
            List<com.vcampus.server.core.library.entity.core.BookCategory> categories = 
                com.vcampus.server.core.library.dao.BookCategoryDao.getInstance().findAll();
            
            // 转换为Map格式
            List<Map<String, Object>> categoryMaps = new ArrayList<>();
            for (com.vcampus.server.core.library.entity.core.BookCategory category : categories) {
                Map<String, Object> categoryMap = new HashMap<>();
                categoryMap.put("categoryCode", category.getCategoryCode());
                categoryMap.put("categoryName", category.getCategoryName());
                categoryMap.put("description", category.getDescription());
                categoryMap.put("sortOrder", category.getSortOrder());
                categoryMaps.add(categoryMap);
            }
            
            log.info("获取所有分类列表成功: count={}", categoryMaps.size());
            return categoryMaps;
            
        } catch (Exception e) {
            log.error("获取所有分类列表异常", e);
            // 返回默认分类列表
            return getDefaultCategories();
        }
    }
    
    /**
     * 获取默认分类列表（当数据库查询失败时使用）
     */
    private List<Map<String, Object>> getDefaultCategories() {
        List<Map<String, Object>> defaultCategories = new ArrayList<>();
        
        String[][] categoryData = {
            {"A", "马克思主义、列宁主义、毛泽东思想、邓小平理论", "马克思主义理论类", "1"},
            {"B", "哲学、宗教", "哲学宗教类", "2"},
            {"C", "社会科学总论", "社会科学总论类", "3"},
            {"D", "政治、法律", "政治法律类", "4"},
            {"E", "军事", "军事类", "5"},
            {"F", "经济", "经济类", "6"},
            {"G", "文化、科学、教育、体育", "文化教育类", "7"},
            {"H", "语言、文字", "语言文字类", "8"},
            {"I", "文学", "文学类", "9"},
            {"J", "艺术", "艺术类", "10"},
            {"K", "历史、地理", "历史地理类", "11"},
            {"N", "自然科学总论", "自然科学总论类", "12"},
            {"O", "数理科学和化学", "数理化学类", "13"},
            {"P", "天文学、地球科学", "天文地球科学类", "14"},
            {"Q", "生物科学", "生物科学类", "15"},
            {"R", "医药、卫生", "医药卫生类", "16"},
            {"S", "农业科学", "农业科学类", "17"},
            {"T", "工业技术", "工业技术类", "18"},
            {"U", "交通运输", "交通运输类", "19"},
            {"V", "航空、航天", "航空航天类", "20"},
            {"X", "环境科学、安全科学", "环境安全科学类", "21"},
            {"Z", "综合性图书", "综合性图书类", "22"}
        };
        
        for (String[] data : categoryData) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("categoryCode", data[0]);
            categoryMap.put("categoryName", data[1]);
            categoryMap.put("description", data[2]);
            categoryMap.put("sortOrder", data[3]);
            defaultCategories.add(categoryMap);
        }
        
        log.info("使用默认分类列表: count={}", defaultCategories.size());
        return defaultCategories;
    }
}