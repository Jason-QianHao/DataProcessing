package com.qian.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.util.IOUtils;
import com.mathworks.toolbox.javabuilder.MWException;
import com.qian.matlab.ReadAndLpf;

@Service
public class DataService {

	// 考虑到实验阶段，这里不对访问加锁。
	// 当需要并发需求时，可以从数据库读取。
	public int MotivationCnt = 5;
	private CountDownLatch countDownLatch_split;
	private CountDownLatch countDownLatch_bp;
	private CountDownLatch countDownLatch_check;
	private String name = "qh";

	/*
	 * 从上位机获取数据service
	 */
	public void getData(@RequestParam(value = "file", required = false) List<MultipartFile> file,
			HttpServletRequest req) {
		try {
			name = req.getParameter("name");
			if(name.equals("")) {
				return;
			}
			// String path = req.getSession().getServletContext().getRealPath("/text");
			// C:\Users\Dell\AppData\Local\Temp\tomcat-docbase.198312894752291490.80\text
			String path = "F:/QH_softDownload/Python/pycharm/work/DigitalArray/addData/train/";

			int count = 1;
			for (MultipartFile f : file) {
				String resourceUrl = name + "_" + MotivationCnt + "_ch" + count++ + ".txt";
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(path + resourceUrl)));// 保存文件到目录下
				out.write(f.getBytes());
				out.flush();
				out.close();
			}
			// 写入标签集
			BufferedWriter labelout = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("F:/QH_softDownload/Python/pycharm/work/DigitalArray/labels.txt", true), "gbk"));
			// 读取数据
			// 循环取出数据
			// 写入相关文件
			labelout.write(name);
			labelout.newLine();
			// 清楚缓存
			labelout.flush();
			labelout.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			req.setAttribute("error", "添加文件失败");
		}
	}

	/*
	 * 数据1000点校改
	 */
	public void points1000Check() {
		countDownLatch_check = new CountDownLatch(1);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO 自动生成的方法存根
				try {
					System.out.println("1000点校改开始...");
					String exe = "python";
					String command = "F:\\QH_softDownload\\Python\\pycharm\\work\\DigitalArray\\File1000pointsCheck_Server.py";
					String[] cmdArr = new String[] { exe, command };
					Process process = Runtime.getRuntime().exec(cmdArr);
					// 打印输出结果
					BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line;
					while ((line = in.readLine()) != null) {
						System.out.println(line);
					}
					in.close();
					int result = process.waitFor();
					System.out.println("执行结果:" + result);
					System.out.println("1000点校改结束...");
				} catch (IOException e) {
					// e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				} finally {
					countDownLatch_check.countDown();
				}
			}
		}).start();
	}

	/*
	 * 进行数据的分割
	 */
	public void splitData() {
		// 这里如果直接在服务器上运行可以通过线程池开辟线程运行
		// 但是其他客户端，调用完此方法后，服务器运行Python程序，客户端继续往下走，天然的多进程。
		// 调用python时，程序中路径必须为绝对路径
		countDownLatch_split = new CountDownLatch(1);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO 自动生成的方法存根
				try {
					countDownLatch_check.await();
					System.out.println("开始执行split....");
					String exe = "python";
					String command = "F:\\QH_softDownload\\Python\\pycharm\\work\\DigitalArray\\FileSplit_SingleTxt_Server.py";
					String[] cmdArr = new String[] { exe, command };
					Process process = Runtime.getRuntime().exec(cmdArr);
					// 打印输出结果
					BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line;
					while ((line = in.readLine()) != null) {
						System.out.println(line);
					}
					in.close();
					int result = process.waitFor();
					System.out.println("执行结果:" + result);
					System.out.println("split结束...");
				} catch (IOException e) {
					// e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				} finally {
//					if(!name.equals("")) {
//						MotivationCnt++; // 为下一次读取文件做准备						
//					}
					countDownLatch_split.countDown();
				}
			}
		}).start();

	}

	/*
	 * 进行数据样本的训练
	 */
	public void bpNNData() {
		// 这里可以通过线程池开辟线程运行
		/*
		 * bpNN样本训练 从splitData重定向过来，前提条件：
		 * 1 在splitData时，必须存放在对应标签文件夹下 
		   2 在splitData时，将每个样本数量存入对应x.txt文件下
		   5 在样本量5000内时，暂时不用动迭代次数 
		   	实现的功能： 增加一个标签
		 * 
		 * 
		 */
		countDownLatch_bp = new CountDownLatch(1);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO 自动生成的方法存根
				try {
					countDownLatch_split.await();
					System.out.println("开始执行bp....");
					String exe = "python";
					//1000点
					String command = "F:\\QH_softDownload\\Python\\pycharm\\work\\DigitalArray\\bp_9channel_400points_Server.py";
					int size = MotivationCnt;
					if(!name.equals("")) {
						size++;
					}
					String[] cmdArr = new String[] { exe, command, String.valueOf(size) };
					Process process = Runtime.getRuntime().exec(cmdArr);
					// 打印输出结果
					BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line;
					while ((line = in.readLine()) != null) {
						System.out.println(line);
					}
					in.close();
					int result = process.waitFor();
					System.out.println("执行结果:" + result);
					// 初始化下一次访问
					System.out.println("执行bp结束....");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO 自动生成的 catch块
					e.printStackTrace();
				} finally {
					if(!name.equals("")) {
						MotivationCnt++; // 为下一次读取文件做准备						
					}
					countDownLatch_bp.countDown();
				}
			}
		}).start();
	}

	/*
	 * 传送文件，可以为客户端在无网络条件下工作提供基础 也可以直接将结果传送给客户端 1 将权重文件传给客户端 2 将标签结果集传给客户端
	 */
	public void transWeights(HttpServletResponse response, HttpServletRequest request) {
		// TODO 自动生成的方法存根
		String index = "";
		try {
			countDownLatch_bp.await();
			System.out.println("开始传输文件至客户端...");
			BufferedReader in = new BufferedReader(
					new FileReader("F:\\QH_softDownload\\Python\\pycharm\\work\\DigitalArray\\wmindex.txt"));
			index = in.readLine();
			System.out.println(index);
			String fileName = "weights" + index + ".txt";
			String path = "F:\\QH_softDownload\\Python\\pycharm\\work\\DigitalArray\\";
			renameFiles(path + fileName);
			downloadfiles(new String[] { path + "weights.txt", path + "labels.txt" }, request, response, path);
			// if (StringUtils.isNotEmpty(fileName)) {
			// response.setContentType("application/vnd.ms-excel");
			// response.setHeader("Content-disposition", "attachment;filename=" + fileName);
			// try {
			// download(response.getOutputStream(), fileName);
			// } catch (Exception e) {
			// // TODO 自动生成的 catch 块
			// e.printStackTrace();
			// }
			// }

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}

	}

	public void reTransWeights(HttpServletResponse response, HttpServletRequest request) {
		// TODO 自动生成的方法存根
		try {
			// BufferedReader in = new BufferedReader(
			// new
			// FileReader("F:\\QH_softDownload\\Python\\pycharm\\work\\BPNN\\wmindex.txt"));
			// index = in.readLine();
			// System.out.println(index);
			String path = "F:\\QH_softDownload\\Python\\pycharm\\work\\DigitalArray\\";
			downloadfiles(new String[] { path + "weights.txt", path + "labels.txt" }, request, response, path);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void download(OutputStream os, String fileName) {
		try {
			// 获取服务器文件
			File file = new File("F:\\QH_softDownload\\Python\\pycharm\\work\\DigitalArray\\" + fileName);

			InputStream ins;
			ins = new FileInputStream(file);
			byte[] b = new byte[1024 * 10];
			int len;
			while ((len = ins.read(b)) > 0) {
				os.write(b, 0, len);
			}
			os.flush();
			os.close();
			ins.close();
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

	}

	public void downloadfiles(String[] files, HttpServletRequest request, HttpServletResponse response, String path) {

		String base_name = "files";
		String fileZip = base_name + ".zip"; // 拼接zip文件,之后下载下来的压缩文件的名字
		String filePath = path + fileZip;// 之后用来生成zip文件

		// 创建临时压缩文件
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
			ZipOutputStream zos = new ZipOutputStream(bos);
			ZipEntry ze = null;
			for (int i = 0; i < files.length; i++) {// 将所有需要下载的文件都写入临时zip文件
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(files[i]));
				ze = new ZipEntry(files[i].substring(files[i].lastIndexOf("\\")));
				zos.putNextEntry(ze);
				int s = -1;
				while ((s = bis.read()) != -1) {
					zos.write(s);
				}
				bis.close();
			}
			zos.flush();
			zos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 以上，临时压缩文件创建完成

		// 进行浏览器下载
		// 获得浏览器代理信息
		String agent = request.getHeader("User-Agent").toUpperCase();
		// 判断浏览器代理并分别设置响应给浏览器的编码格式
		String finalFileName = null;
		if ((agent.indexOf("MSIE") > 0) || ((agent.indexOf("RV") != -1) && (agent.indexOf("FIREFOX") == -1)))
			try {
				finalFileName = URLEncoder.encode(fileZip, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		else {
			try {
				finalFileName = new String(fileZip.getBytes("UTF-8"), "ISO8859-1");
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		response.setContentType("application/x-download");// 告知浏览器下载文件，而不是直接打开，浏览器默认为打开
		response.setHeader("Content-Disposition", "attachment;filename=\"" + finalFileName + "\"");// 下载文件的名称
		// 输出到本地
		try {
			ServletOutputStream servletOutputStream = response.getOutputStream();
			DataOutputStream temps = new DataOutputStream(servletOutputStream);

			DataInputStream in = new DataInputStream(new FileInputStream(filePath));// 浏览器下载临时文件的路径
			byte[] b = new byte[1024 * 10];
			File reportZip = new File(filePath);// 之后用来删除临时压缩文件
			try {
				while ((in.read(b)) != -1) {
					temps.write(b);
				}
				temps.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (temps != null)
					temps.close();
				if (in != null)
					in.close();
				if (reportZip != null)
					reportZip.delete();// 删除服务器本地产生的临时压缩文件
				servletOutputStream.close();

			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	/*
	 * 重命名文件
	 */
	public static void renameFiles(String fileName) {
		String newFilename = "F:\\QH_softDownload\\Python\\pycharm\\work\\DigitalArray\\weights.txt";
		File target = new File(newFilename);
		if (target.exists()) { // 新文件若存在，则删掉
			target.delete();
		}
		File file = new File(fileName); // 旧文件
		boolean result = file.renameTo(target); // 将旧文件更名
		if (result) {
			System.out.println("文件直接更名成功: " + fileName + " -> " + newFilename);
		} else { // 更名失败，则采取变相的更名方法
			copyFile(file, target); // 将旧文件拷贝给新文件
			System.out.println("将旧文件拷贝给新文件  " + fileName + " -> " + newFilename);
		}
	}

	// 复制文件
	public static void copyFile(File sourceFile, File targetFile) {
		try {
			// 新建文件输入流并对它进行缓冲
			FileInputStream input = new FileInputStream(sourceFile);
			BufferedInputStream inBuff = new BufferedInputStream(input);
			// 新建文件输出流并对它进行缓冲
			FileOutputStream output = new FileOutputStream(targetFile);
			BufferedOutputStream outBuff = new BufferedOutputStream(output);
			// 缓冲数组
			byte[] b = new byte[1024 * 10];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outBuff.flush();
			// 关闭流
			inBuff.close();
			outBuff.close();
			output.close();
			input.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Failed to rename ");
			e.printStackTrace();
		}

	}

}
