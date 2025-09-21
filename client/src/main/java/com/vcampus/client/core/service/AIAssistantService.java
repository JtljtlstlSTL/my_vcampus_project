package com.vcampus.client.core.service;

import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import okhttp3.*;
import java.io.IOException;

public class AIAssistantService {
	// 多轮对话上下文（简单实现：只记住最近几条）
	private List<String> conversationHistory = new ArrayList<>();
	// 用户偏好（如身份、常用功能）
	private String userRoleMemory = "";
	private List<String> userFavoriteFeatures = new ArrayList<>();

	/**
	 * 记录用户对话内容
	 */
	public void addToConversationHistory(String userInput, String aiReply) {
		if (conversationHistory.size() > 10) conversationHistory.remove(0);
		conversationHistory.add("用户: " + userInput);
		conversationHistory.add("AI: " + aiReply);
	}

	/**
	 * 获取最近对话历史
	 */
	public String getRecentConversation() {
		StringBuilder sb = new StringBuilder();
		for (String line : conversationHistory) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}

	/**
	 * 记住用户身份和常用功能
	 */
	public void rememberUserRole(String role) {
		userRoleMemory = role;
	}
	public String getUserRoleMemory() {
		return userRoleMemory;
	}
	public void addUserFavoriteFeature(String feature) {
		if (!userFavoriteFeatures.contains(feature)) userFavoriteFeatures.add(feature);
	}
	public List<String> getUserFavoriteFeatures() {
		return userFavoriteFeatures;
	}

	/**
	 * 数据分析与报表自动生成（示例：成绩报表、借阅统计）
	 * 实际可对接数据库或业务模块，这里仅示例格式
	 */
	public String generateReport(String type, String userId) {
		if (type.equalsIgnoreCase("成绩")) {
			// 示例：生成成绩报表
			return "成绩报表\n----------------\n语文：90\n数学：88\n英语：92\nGPA：3.8\n----------------\n如需导出，请点击‘导出’按钮。";
		}
		if (type.equalsIgnoreCase("借阅")) {
			// 示例：生成借阅统计
			return "借阅统计\n----------------\n本月借阅：5本\n历史总借阅：32本\n逾期：0本\n----------------\n如需详细清单，请前往‘我的借阅’。";
		}
		return "暂不支持该类型报表，请输入‘成绩报表’或‘借阅统计’。";
	}
	// DeepSeek API Key（请替换为你的真实API Key）
	private static final String API_KEY = "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh";
	private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
	private final OkHttpClient client = new OkHttpClient.Builder()
		.connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
		.readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
		.build();

	/**
	 * 获取学生端功能列表
	 */
	public List<String> getStudentFeatures() {
		List<String> features = new ArrayList<>();
		features.add("加权平均分和GPA计算");
		features.add("选课与成绩查询");
		features.add("图书馆借阅与归还");
		features.add("校园卡充值与消费");
		features.add("商店购物");
		features.add("个人信息管理");
		return features;
	}

	/**
	 * 获取教师端功能列表
	 */
	public List<String> getTeacherFeatures() {
		List<String> features = new ArrayList<>();
		features.add("课程管理与成绩录入");
		features.add("学生信息查询");
		features.add("批量导入成绩");
		features.add("图书馆管理");
		features.add("校园卡管理");
		features.add("商店管理");
		features.add("个人信息管理");
		return features;
	}

	/**
	 * 自动读取FEATURES_README.md并解析功能点
	 * @param role "student" 或 "teacher"
	 */
	public List<String> getFeaturesFromReadme(String role) {
		List<String> features = new ArrayList<>();
		String filePath = "d:/Users/LEGION/Downloads/my_vcampus_project/client/src/main/java/com/vcampus/client/core/service/FEATURES_README.md";
		try {
			List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
			boolean inSection = false;
			String sectionHeader = role.equalsIgnoreCase("teacher") ? "## 教师端功能" : "## 学生端功能";
			for (String line : lines) {
				if (line.trim().equals(sectionHeader)) {
					inSection = true;
					continue;
				}
				if (inSection) {
					if (line.startsWith("## ")) break; // 下一个section
					if (line.startsWith("- ")) {
						features.add(line.substring(2).trim());
					}
				}
			}
		} catch (Exception e) {
			System.err.println("[AI助手] 读取FEATURES_README失败: " + e.getMessage());
		}
		return features;
	}

	/**
	 * 智能处理用户输入，专注于项目相关内容的对话和事务引导
	 * @param query 用户输入
	 * @param role 用户角色（"student" 或 "teacher"）
	 * @return 智能回复
	 */
	public String handleUserQuery(String query, String role) {
	// 记忆用户身份
	rememberUserRole(role);
	// 多轮对话记忆：记录本次对话
	String aiReply = null;
		if (query == null || query.trim().isEmpty()) {
			return "请先输入您的问题或需求。";
		}
		String lower = query.toLowerCase();
		if (!role.equalsIgnoreCase("teacher") && !role.equalsIgnoreCase("student")) {
			return "请确认您的身份（教师或学生），以便获得正确的功能指引。";
		}
		List<String> features = getFeaturesFromReadme(role);
		if (features.isEmpty()) {
			features = role.equalsIgnoreCase("teacher") ? getTeacherFeatures() : getStudentFeatures();
		}
		if (lower.contains("能做什么") || lower.contains("功能") || lower.contains("介绍")) {
			StringBuilder sb = new StringBuilder();
			sb.append(role.equalsIgnoreCase("teacher") ? "教师端" : "学生端").append("支持以下功能：\n");
			for (String feature : features) {
				sb.append(feature).append("\n");
			}
			sb.append("请直接描述你想进行的操作，我会一步步为你详细说明。");
			aiReply = sb.toString().replace("\\n", "\n");
			// 统一处理回复内容，去除所有\n、**、*、`等Markdown和转义字符
			String cleanReply = aiReply.replace("\\n", "\n").replace("\n", "\n")
				.replace("**", "").replace("*", "").replace("`", "");
			addToConversationHistory(query, cleanReply);
			return cleanReply;
		}
		// 智能匹配功能关键词和事务引导
		for (String feature : features) {
			String keyword = feature.replace("与","").replace("管理","").replace("功能","").replace("、","").replace("，","").replace("：","").toLowerCase();
			if (lower.contains(keyword)) {
				addUserFavoriteFeature(feature);
				// 事务引导示例
				if (keyword.contains("借阅") || keyword.contains("图书馆")) {
					aiReply = "图书馆借阅与归还：\n1. 打开主界面，点击‘图书借阅’模块。\n2. 搜索你想借的图书，点击‘借阅’按钮。\n3. 归还图书请在‘我的借阅’中选择要归还的书籍，点击‘归还’。".replace("\\n", "\n");
					addToConversationHistory(query, aiReply);
					return aiReply;
				}
				if (keyword.contains("选课")) {
					aiReply = "选课与成绩查询：\n1. 进入‘课程管理’模块，浏览课程列表。\n2. 选中目标课程，点击‘选课’。\n3. 成绩查询请进入‘课程管理中的成绩管理’模块，查看已修课程成绩。".replace("\\n", "\n");
					addToConversationHistory(query, aiReply);
					return aiReply;
				}
				if (keyword.contains("充值") || keyword.contains("校园卡")) {
					aiReply = "校园卡充值与消费：\n1. 打开‘校园卡’模块。\n2. 点击‘充值’，输入金额并确认。\n3. 消费记录可在‘消费明细’中查看。".replace("\\n", "\n");
					addToConversationHistory(query, aiReply);
					return aiReply;
				}
				if (keyword.contains("购物") || keyword.contains("商店")) {
					aiReply = "商店购物：\n1. 进入‘校园商店’模块，浏览商品。\n2. 选中商品后点击‘购买’，填写收货信息并确认订单。\n3. 订单可在‘我的订单’中管理和查询。".replace("\\n", "\n");
					addToConversationHistory(query, aiReply);
					return aiReply;
				}
				if (keyword.contains("成绩录入") || keyword.contains("批量导入")) {
					aiReply = "成绩录入与批量导入：\n1. 教师进入‘成绩管理’模块，选择‘批量导入’。\n2. 上传Excel文件，系统自动录入成绩。\n3. 可在成绩列表中核查和修改。".replace("\\n", "\n");
					addToConversationHistory(query, aiReply);
					return aiReply;
				}
				// 默认事务引导
				aiReply = feature + "操作请直接描述你的具体需求，我会为你详细说明每一步。";
				addToConversationHistory(query, aiReply);
				return aiReply;
			}
		}
		// 保留原有特殊匹配
		if (lower.contains("gpa") || lower.contains("平均分")) {
			aiReply = "加权平均分和GPA计算：\n1. 进入‘成绩管理’模块。\n2. 查看所有课程成绩，系统自动计算加权平均分和GPA。\n3. 如需导出成绩，可点击‘导出’按钮。".replace("\\n", "\n");
			addToConversationHistory(query, aiReply);
			return aiReply;
		}
		if (lower.contains("批量导入")) {
			aiReply = "批量导入成绩：\n1. 教师进入‘成绩管理’模块，点击‘批量导入’。\n2. 上传Excel文件，系统自动录入所有学生成绩。".replace("\\n", "\n");
			addToConversationHistory(query, aiReply);
			return aiReply;
		}
		if (lower.contains("数据库") || lower.contains("查询")) {
			aiReply = "数据库查询与管理：\n1. 进入‘数据库管理’模块。\n2. 选择要查询的数据表，输入查询条件。\n3. 点击‘查询’按钮，结果会在下方显示。".replace("\\n", "\n");
			addToConversationHistory(query, aiReply);
			return aiReply;
		}
		// 智能报表请求
		if (lower.contains("成绩报表")) {
			aiReply = generateReport("成绩", "当前用户ID");
			addToConversationHistory(query, aiReply);
			return aiReply;
		}
		if (lower.contains("借阅统计") || lower.contains("借阅报表")) {
			aiReply = generateReport("借阅", "当前用户ID");
			addToConversationHistory(query, aiReply);
			return aiReply;
		}
		// 默认回复
		aiReply = "请描述你想了解的" + (role.equalsIgnoreCase("teacher") ? "教师端" : "学生端") + "功能或操作，我会为你提供专属引导。";
		addToConversationHistory(query, aiReply);
		return aiReply;
	}
	/**
	 * 向DeepSeek AI助手发送消息，获取回复
	 * @param message 用户输入
	 * @return AI回复
	 */
	public String sendMessageToAI(String message) {
		if (message == null || message.trim().isEmpty()) {
			return "请先输入内容。";
		}
		String systemPrompt = "您好，我是校园项目智能助手。请根据您的需求提问，我会结合本系统功能为您提供具体操作指引。例如：如需借书，请在学生主界面选择‘图书管理’标签，进入后可搜索并借阅图书；如需修改密码，请点击主界面的‘修改密码’按钮，输入新密码后确认即可。无论是成绩查询、课程选课、校园卡充值、个人信息修改等操作，您都可以直接描述需求，我会为您详细说明对应的步骤和入口。";
		String json = "{" +
			"\"model\":\"deepseek-chat\"," + 
			"\"messages\":[" + 
			"{\"role\":\"system\",\"content\":\"" + systemPrompt + "\"}," + 
			"{\"role\":\"user\",\"content\":\"" + message.replace("\"", "\\\"") + "\"}" + 
			"]," + 
			"\"stream\":false" + 
			"}";
		try {
			RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
			Request request = new Request.Builder()
					.url(API_URL)
					.addHeader("Authorization", "Bearer " + API_KEY)
					.post(body)
					.build();
			try (Response response = client.newCall(request).execute()) {
				String resp = response.body().string();
				if (!response.isSuccessful()) {
					return "AI服务请求失败：" + response.code();
				}
				int idx = resp.indexOf("\"content\":");
				if (idx != -1) {
					int start = resp.indexOf('"', idx + 10) + 1;
					int end = resp.indexOf('"', start);
					if (start > 0 && end > start) {
						String raw = resp.substring(start, end);
						// 统一处理AI回复内容
						String clean = raw.replace("\\n", System.lineSeparator())
							.replace("\n", System.lineSeparator())
							.replace("**", "").replace("*", "").replace("`", "");
						return clean;
					}
				}
				return "AI回复解析失败：" + resp;
			}
		} catch (IOException e) {
			System.err.println("[AI助手] 网络异常: " + e.getMessage());
			return "AI服务异常：" + e.getMessage();
		}
	}
}
