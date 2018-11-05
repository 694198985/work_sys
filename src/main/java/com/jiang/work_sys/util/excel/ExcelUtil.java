package com.jiang.work_sys.util.excel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {

	public static List<List<List<String>>> readExcel(String path) throws InvalidFormatException, IOException {
		return readExcel(path, null);
	}

	public static List<List<List<String>>> readExcel(String path, int[] firstRowIndex)
			throws InvalidFormatException, IOException {
		// -----sheet表格list
		List<List<List<String>>> sheetsList = new ArrayList<>();
		File file = new File(path);
		// 判断文件是否存在
		if (file.isFile() && file.exists()) {
			Workbook wb = getWorkbok(file);
			if (wb == null) {
				throw new InvalidFormatException("excel格式错误！！！！");
			}
			int sheets_num = wb.getNumberOfSheets();
			for (int i = 0; i < sheets_num; i++) {
				// -----每个sheet表格的行数
				List<List<String>> rowsList = new ArrayList<>();
				// 开始解析
				int tempIndex = 0;
				if (firstRowIndex != null && firstRowIndex.length != 0) {
					if (firstRowIndex.length > i) {
						tempIndex = firstRowIndex[i];
					}
				}
				goRead(rowsList, i, tempIndex, wb);
				sheetsList.add(rowsList);
			}
		} else {
			throw new RuntimeException("文件不存在！");
		}
		return sheetsList;
	}

	public static List<List<String>> readExcel(String path, int sheetIndex, int firstRowIndex)
			throws InvalidFormatException, IOException {
		// -----sheet表格list
		File file = new File(path);
		// -----每个sheet表格的行数
		List<List<String>> rowsList = new ArrayList<>();
		// 判断文件是否存在
		if (file.isFile() && file.exists()) {
			Workbook wb = getWorkbok(file);
			goRead(rowsList, sheetIndex, firstRowIndex, wb);
		} else {
			throw new RuntimeException("文件不存在！");
		}
		return rowsList;
	}

	public static List<List<String>> readExcel(String path, int sheetIndex) throws InvalidFormatException, IOException {
		return readExcel(path, sheetIndex, 0);
	}

	public static void createExcel(List<List<List<String>>> sheetList, OutputStream ostream) throws IOException {
		createExcel(sheetList, ostream, ExcelEnum.EXCEL_XLSX, ExcelEnum.BASE_TEMPLATE);
	}

	public static void createExcel(List<List<List<String>>> sheetList, OutputStream ostream, ExcelEnum postfix,
			ExcelEnum template) throws IOException {
		Workbook wb = null;
		try {
			// 定义一个Excel表格
			wb = getWorkbok(postfix);
			// ---------------------------------------------输出对应的模板
			ExcelExportFactory.getExportTemplate(template).exportExcel(wb, sheetList);
			// --------------------------------------------end
			// 输出流,下载时候的位置
			wb.write(ostream);
		} finally {
			if (ostream != null) {
				try {
					ostream.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					ostream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (wb != null) {
				try {
					wb.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @Title: goRead
	 * @Description:
	 * @param rowsList
	 *            行
	 * @param sheetIndex
	 *            表
	 * @param firstRowIndex
	 *            第一行
	 * @param wb
	 * @throws InvalidFormatException
	 * @return void 返回类型
	 * @author lyw
	 * @date 2018年10月30日下午2:25:28
	 */
	private static void goRead(List<List<String>> rowsList, int sheetIndex, int firstRowIndex, Workbook wb)
			throws InvalidFormatException {
		if (wb == null) {
			throw new InvalidFormatException("excel格式错误！！！！");
		}
		// 开始解析
		Sheet sheet = wb.getSheetAt(sheetIndex);
		// 第一行是列名，所以从第二行开始遍历
		int firstRowNum = sheet.getFirstRowNum() + firstRowIndex;
		int lastRowNum = sheet.getLastRowNum();
		// 遍历行
		for (int rIndex = firstRowNum; rIndex <= lastRowNum; rIndex++) {
			// 获取当前行的内容
			Row row = sheet.getRow(rIndex);
			if (row != null) {
				// ---列数
				List<String> cellsList = new ArrayList<>();
				int firstCellNum = row.getFirstCellNum();
				int lastCellNum = row.getLastCellNum();
				for (int cIndex = firstCellNum; cIndex < lastCellNum; cIndex++) {
					Cell cell = row.getCell(cIndex);
					String value = "";
					if (cell != null) {
						if (cell.getCellTypeEnum().equals(CellType.NUMERIC)) {
							if (DateUtil.isCellDateFormatted(cell)) {
								value = new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
							} else {
								cell.setCellType(CellType.STRING);
								// 获取单元格的值
								value = cell.getStringCellValue();
								// double cellValD = cell.getNumericCellValue();
								// long cellValL =
								// Double.valueOf(cellValD).longValue();
								// if (cellValD == cellValL) {
								// value = cellValL + "";
								// } else {
								//
								// value = DecimalFormatTwo(cellValD) + "";
								// }
							}
						}
						// else if
						// (cell.getCellTypeEnum().equals(CellType.FORMULA)) {
						// value = cell.getCellFormula();
						// }
						else {
							cell.setCellType(CellType.STRING);
							// 获取单元格的值
							value = cell.getStringCellValue();
						}
					}
					// ---将列值添加到列的list集合
					cellsList.add(value);
				}
				rowsList.add(cellsList);
			}
		}
	}

	private static Workbook getWorkbok(File file) throws InvalidFormatException, IOException {
		return WorkbookFactory.create(file);
	}

	private static Workbook getWorkbok(ExcelEnum excelEnum) {
		if (ExcelEnum.EXCEL_XLS.equals(excelEnum)) {
			return new HSSFWorkbook();
		} else {
			return new XSSFWorkbook();
		}
	}

	// private static String DecimalFormatTwo(Double val) {
	// DecimalFormat df = new DecimalFormat("0.000");
	// return df.format(val);
	// }

}
