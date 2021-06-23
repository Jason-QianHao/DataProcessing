package com.qian.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.qian.service.DataService;
import com.qian.util.HttpClient;

@Controller
public class DataController {

	@Autowired
	private DataService dataService;
	/*
	 * 从上位机获取页面controller
	 */
	@RequestMapping("/DigitalArray")
	public String index() {
		return "addTrainData";
	}	
	@RequestMapping("/getData")
	public String getData(@RequestParam(value = "file", required = false) List<MultipartFile> file, HttpServletRequest req) {
		dataService.getData(file, req);
		return "redirect:/points1000Check";
	}
	/*
	 * 数据1000点校改
	 */
	@RequestMapping("/points1000Check")
	public String points1000Check() {
		dataService.points1000Check();
		return "redirect:/splitData";
	}
	/*
	 * 进行数据的分割
	 */
	@RequestMapping("/splitData")
	public String splitData(HttpServletResponse resp) {
		dataService.splitData();
		return "redirect:/bpNNData";
//		return "splitData successgfully!";
	}
	/*
	 * 进行数据样本的训练
	 */
	@RequestMapping("/bpNNData")
	public String bpNNData(HttpServletResponse resp) {
		dataService.bpNNData();
		return "redirect:/downloadFile";
		//"BPNN is excuted successfully!"
	}
	/*
	 * 将权重文件传给客户端
	 */
	@RequestMapping("/downloadFile")
	public String downloadFile() {
		return "success";
	}
	@RequestMapping("/transWeights")
	public void transWeights(HttpServletResponse response, HttpServletRequest req) {
			dataService.transWeights(response, req);
	}
	@RequestMapping("/reTransWeights")
	public void reTransWeights(HttpServletResponse response, HttpServletRequest req) {
		dataService.reTransWeights(response, req);
	}
}
