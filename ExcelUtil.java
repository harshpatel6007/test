package com.slktechlabs.hmis.system.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.slktechlabs.hmis.system.util.Excel.AllowNull;
import com.slktechlabs.hmis.system.util.Excel.Column;
import com.slktechlabs.hmis.system.util.Excel.ExcelDescription;
import com.slktechlabs.hmis.system.util.Excel.ExcelTitle;
import com.slktechlabs.hmis.system.util.Excel.Header;
import com.slktechlabs.hmis.system.util.Excel.SkipRow;

public class ExcelUtil {

	private static Logger logger = Logger.getLogger(ExcelUtil.class);
	
	public static boolean isEmptyRow(Row row) {
		boolean isEmpty = true;
		if (row.getLastCellNum() <= 0) { 
			return true;
			}
		for (int cellnum = row.getFirstCellNum(); cellnum <= row
				.getLastCellNum(); cellnum++) {
			System.out.println("cellnum :: " +cellnum);
			
			Cell cell = row.getCell(cellnum);
			if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
				isEmpty = false;
			}
		}
		return isEmpty;
	}

	public static <T> List<T> iterateLimitedRow(Iterator<Row> rowIterator, 
			Class<T> klass, int limit) {
		List<T> list = new ArrayList<T>();
		
		int skip = getRowToSkip(klass);
		int i = 0;
		while (rowIterator.hasNext()) {
			i++;
			Row row = (Row) rowIterator.next();
			if (ExcelUtil.isEmptyRow(row) || i <= skip) {
				continue;
			}
			T t = (T) getObjectFromRow(row, klass);
			list.add(t);
			if(i == limit) {
				break;
			}
		}
		
		return list;
	}
	
	public static <T> List<T> iterateRow(Iterator<Row> rowIterator, Class<T> klass) {
		List<T> list = new ArrayList<T>();
		
		int skip = getRowToSkip(klass);
		int i = 0;
		while (rowIterator.hasNext()) {
			i++;
			Row row = (Row) rowIterator.next();
			if (ExcelUtil.isEmptyRow(row) || i <= skip) {
				continue;
			}
			T t = (T) getObjectFromRow(row, klass);
			list.add(t);
		}
		
		return list;
	}
	
	private static <T> int getRowToSkip(Class<T> klass) {
		if(klass.isAnnotationPresent(SkipRow.class)) {
			return klass.getAnnotation(SkipRow.class).skip();
		} 
		return 0;
	}
	
	private static <T, V> Object getObjectFromRow(Row row, Class<T> klass) {

		Object object = createObjectFromClass(klass);

		for (Field field : klass.getDeclaredFields()) {
			if (!field.isAnnotationPresent(Column.class)) {
				throw new IllegalArgumentException("Column annotation not "
						+ "found on field " + field.getName());
			}

			Object value = readValue(row, field, klass);

			try {
				new PropertyDescriptor(field.getName(), klass).getWriteMethod()
						.invoke(object, value);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | IntrospectionException e) {
				logger.error(e.getMessage(), e);
				throw new IllegalArgumentException("Error Occured during invoke setter method for "
						+ field.getName() + " of object" + object);
			}
		}

		return object;
	}

	private static <T> Object createObjectFromClass(Class<T> klass) {
		Object object = null;
		try {
			Class<?> clazz = Class.forName(klass.getName());
			Constructor<?> ctor = clazz.getConstructor();
			object = ctor.newInstance(new Object[] {});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new IllegalArgumentException("Error Occured during Create Object "
					+ "From Class");
		}
		return object;
	}

	private static <T> Object readValue(Row row, Field field, Class<T> klass) {
		Class<T> clazz = (Class<T>) field.getType();
		Column column = field.getAnnotation(Column.class);
		int index = column.index();
		
		if(row.getCell(index) == null || row.getCell(index).getCellType() == Cell.CELL_TYPE_BLANK) {
			if(klass.isAnnotationPresent(AllowNull.class)) {
				return null;
			}
			throw new IllegalArgumentException("Value not exists for field " + 
					field.getName() + " on index " + index);
		}

		if (clazz.isAssignableFrom(String.class)
				|| clazz.isAssignableFrom(Character.class)
				|| clazz.equals("char")) {
			return row.getCell(index).getStringCellValue();
		}

		if (clazz.equals(Boolean.class) || clazz.equals("boolean")) {
			return row.getCell(index).getBooleanCellValue();
		}

		if (clazz.isAssignableFrom(Double.class) || clazz.equals("double")
				|| clazz.isAssignableFrom(Long.class) || clazz.equals("long")
				|| clazz.isAssignableFrom(Float.class) || clazz.equals("float")) {
			return row.getCell(index).getNumericCellValue();
		}

		if (clazz.isAssignableFrom(Integer.class)
				|| clazz.getName().equals("int")) {
			return (int) row.getCell(index).getNumericCellValue();
		}

		if (clazz.isAssignableFrom(Byte.class) || clazz.equals("byte")) {
			return (byte) row.getCell(index).getNumericCellValue();
		}

		if (clazz.isAssignableFrom(Short.class) || clazz.equals("short")) {
			return (short) row.getCell(index).getNumericCellValue();
		}

		return null;
	}
	
	public static <T, V> void createExcelFile(Collection<T> data,
			File file, Class<T> klass, JSONObject json, Class<V> filterClass) {

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet();
		
		int createdRow = 0;
		if(klass.isAnnotationPresent(ExcelTitle.class)) {
			addSheetTitle(klass, sheet);
			createdRow++;
		}
		
		if(json != null) {
			addFilterDetail(json, sheet, filterClass, createdRow);
			createdRow += filterClass.getDeclaredFields().length;
			createdRow++;
		}
		
		createdRow = createHeaderRow(sheet, klass, createdRow + 1);
		addContentToExcel(data, sheet, createdRow);
		
		try {
			FileOutputStream out = new FileOutputStream(file);
			workbook.write(out);
			out.close();
			workbook.close();
		} catch (IOException e) {
			logger.error("Error Occured During close Workbook", e);
		}
	}

	private static <T> void addFilterDetail(JSONObject json, XSSFSheet sheet,
			Class<T> filterClass, int rowCount) {
		
		
		for(Field field : filterClass.getDeclaredFields()) {
			
			try {
				if(json.has(field.getName())) {
					XSSFRow row = sheet.createRow(++rowCount);
					Object value = json.get(field.getName());
					XSSFCell cell = row.createCell(0);
					cell.setCellStyle(getTitleRowStyle(sheet.getWorkbook()));
					cell.setCellValue(getFieldName(field));
					XSSFCell cell1 = row.createCell(1);
					cell1.setCellStyle(getTitleRowStyle(sheet.getWorkbook()));
					System.out.println("cretes cell for filed : " + field.getName() 
							+ " value : " + value);
					/*if(value != null) {
					}*/
					insertValueInCell(field, value, cell1);
				}
			} catch (JSONException e) {
				logger.error("error occured during read json object", e);
			}
			
		}
	}

	private static String getFieldName(Field field) {
		if(field.isAnnotationPresent(ExcelDescription.class)) {
			return field.getAnnotation(ExcelDescription.class).value();
		}
		return field.getName();
	}

	private static <T> void addContentToExcel(Collection<T> data,
			XSSFSheet sheet, int rowIndex) {
		for(T t : data) {
			writeDataInRow(t, sheet, rowIndex);
			rowIndex++;
		}
	}
	
	private static <T> void writeDataInRow(T t, XSSFSheet sheet, int rowIndex) {
		
		XSSFRow row = sheet.createRow(rowIndex);
		
		int i = 0;
		for (Field field : t.getClass().getDeclaredFields()) {
			Object value = null;
			try {
				value = new PropertyDescriptor(field.getName(), 
						t.getClass()).getReadMethod().invoke(t);
				
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | IntrospectionException e) {
				System.out.println("error occured during read value for field : "
					+ field.getName());
				e.printStackTrace();
			}
			
			XSSFCell cell = row.createCell(i);
			cell.setCellStyle(getDefaultStyleForTable(sheet));
				insertValueInCell(field, value, cell);
			i++;
		}
	}
	
	private static void insertValueInCell(Field field, Object value,
			XSSFCell cell) {
		if(value != null) {
			if (field.getType().isAssignableFrom(String.class)
					|| field.getType().isAssignableFrom(Character.class)
					|| field.getType().equals("char")) {
				cell.setCellType(XSSFCell.CELL_TYPE_STRING);
				cell.setCellValue((String)value);
			}

			if (field.getType().equals(Boolean.class) || field.getType().equals("boolean")) {
				cell.setCellType(XSSFCell.CELL_TYPE_BOOLEAN);
				cell.setCellValue(MyUtils.getBooleanFromValue((Boolean)value));
			}

			if (field.getType().isAssignableFrom(Double.class) || field.getType().equals("double")
					|| field.getType().isAssignableFrom(Long.class) || field.getType().equals("long")
					|| field.getType().isAssignableFrom(Float.class) || field.getType().equals("float")) {
				cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellValue((double)value);
			}

			if (field.getType().isAssignableFrom(Integer.class)
					|| field.getType().getName().equals("int")) {
				cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellValue((int)value);
			}

			if (field.getType().isAssignableFrom(Byte.class) || field.getType().equals("byte")) {
				cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellValue((byte)value);
			}

			if (field.getType().isAssignableFrom(Short.class) || field.getType().equals("short")) {
				cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellValue((short)value);
			}
			
			if (field.getType().isAssignableFrom(Date.class)) {
				if(value instanceof Long){
					Date date = new Date((long)value);
					cell.setCellValue(DateUtils.getDefaultDateTimeFormat3(date));
				}else{
					Date date = (Date) value;
					cell.setCellValue(DateUtils.getDefaultDateTimeFormat3(date));
				}	
			}
			
			if(Collection.class.isAssignableFrom(field.getType())) {
				if(value instanceof JSONArray) {
					ArrayList<String> list = new ArrayList<String>();     
					JSONArray jsonArray = (JSONArray)value; 
					if (jsonArray != null) { 
					   int len = jsonArray.length();
					   for (int i=0;i<len;i++){ 
						   try {
							list.add(jsonArray.get(i).toString());
						} catch (JSONException e) {
							logger.error("Error occured during get value form json"
									+ " array", e);
						}
					   } 
					} 
					cell.setCellValue(org.springframework.util.StringUtils
							.collectionToCommaDelimitedString(list));
				} else {
					cell.setCellValue(org.springframework.util.StringUtils
							.collectionToCommaDelimitedString((Collection<?>)value));
				}
			}
		} else {
			cell.setCellValue("-");
		}
		
	}

	private static <T> int createHeaderRow(XSSFSheet sheet, Class<T> klass,
			int rowCount) {
		if(klass.isAnnotationPresent(Header.class)) {
			Header header = klass.getAnnotation(Header.class);
			return createHeaderUsingAnnotation(header, sheet, rowCount);
		} 
		return createHeaderUsingClass(klass, sheet, rowCount);
	}
	
	private static <T> void addSheetTitle(Class<T> klass, XSSFSheet sheet) {
		XSSFRow row = sheet.createRow(0);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue(klass.getAnnotation(ExcelTitle.class).value());
		cell.setCellStyle(getTitleRowStyle(sheet.getWorkbook()));
	}

	private static <T> int createHeaderUsingClass(Class<T> klass, 
			XSSFSheet sheet, int rowCount) {
		XSSFCellStyle cellStyle = getDefaultStyleSheetForHeader(sheet);
		
		XSSFRow row = sheet.createRow(rowCount++);
		
		int i = 0;
		for(Field field : klass.getDeclaredFields()) {
			XSSFCell cell = row.createCell(i);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(field.getName());
			sheet.setColumnWidth(i, 8000);
			i++;
		}
		return rowCount;
	}
	
	private static int createHeaderUsingAnnotation(Header header,
			XSSFSheet sheet, int rowCount) {
		XSSFCellStyle cellStyle = getDefaultStyleSheetForHeader(sheet);
		XSSFRow row = sheet.createRow(rowCount++);
		
		int i = 0;
		for(String str : header.heading()) {
			XSSFCell cell = row.createCell(i);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(str);
			sheet.setColumnWidth(i, 8000);
			i++;
		}
		return rowCount;
	}

	private static XSSFCellStyle getDefaultStyleSheetForHeader(XSSFSheet sheet) {
		XSSFCellStyle style = sheet.getWorkbook().createCellStyle();
		
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
		style.setAlignment(CellStyle.ALIGN_CENTER);
		
		Font font = sheet.getWorkbook().createFont();// Create font
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);// Make font bold
		style.setFont(font);
		
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		
		return style;
	}

	private static CellStyle getDefaultStyleForTable(XSSFSheet sheet) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle = sheet.getWorkbook().createCellStyle();

		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyle.setBorderTop(CellStyle.BORDER_THIN);
		cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		cellStyle.setBorderRight(CellStyle.BORDER_THIN);
		cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		
		return cellStyle;
	}
	
	/**
	 * create title row
	 * 
	 * @param workbook
	 * @return
	 */
	public static CellStyle getTitleRowStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
		font.setFontHeightInPoints((short) 14);
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(IndexedColors.BLACK.index);
		cellStyle.setFont(font);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		cellStyle.setBorderRight(CellStyle.BORDER_THIN);
		cellStyle.setBorderTop(CellStyle.BORDER_THIN);
		return cellStyle;
	}
	
	
	/**
	 * create title row
	 * 
	 * @param workbook
	 * @return
	 */
	public static CellStyle getHeaderStyle(Workbook workbook, int fontSize) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
		font.setFontHeightInPoints((short) fontSize);
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(IndexedColors.BLACK.index);
		cellStyle.setFont(font);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		cellStyle.setBorderRight(CellStyle.BORDER_THIN);
		cellStyle.setBorderTop(CellStyle.BORDER_THIN);
		return cellStyle;
	}
	
	public static CellStyle getColorStyle(Workbook workbook, boolean isForHeader) {
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(isForHeader ? IndexedColors.GREY_25_PERCENT.index : IndexedColors.WHITE.index);
		style.setAlignment(isForHeader ? CellStyle.ALIGN_CENTER : CellStyle.ALIGN_LEFT);
		if (isForHeader) {
			Font font = workbook.createFont();// Create font
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);// Make font bold
			style.setFont(font);
		}
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		return style;
	}
	
	/**
	 * @param title
	 * @param workbook
	 * @param sheet
	 */
	public static void createTitleRow(String title, Workbook workbook,
			Sheet sheet) {
		sheet.shiftRows(0, sheet.getLastRowNum(), 2);
		Row titleRow = sheet.createRow(0);
		titleRow.createCell(0).setCellValue(title);
		titleRow.getCell(0).setCellStyle(getTitleRowStyle(workbook));
		titleRow.setHeight((short) 420);
		// sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colSpanLength -
		// 1));
	}
	
	/**
	 * @param workbook
	 * @return No Record Founds Row Style
	 * @Date :- 6/2/2015
	 */
	public static CellStyle getNoRecordFoundRowStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle = workbook.createCellStyle();
		Font hSSFFont = workbook.createFont();
		hSSFFont.setFontName(XSSFFont.DEFAULT_FONT_NAME);
		hSSFFont.setFontHeightInPoints((short) 11);
		hSSFFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		hSSFFont.setColor(IndexedColors.BLACK.index);
		cellStyle.setFont(hSSFFont);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyle.setBorderTop(CellStyle.BORDER_THIN);
		cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		cellStyle.setBorderRight(CellStyle.BORDER_THIN);
		cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		return cellStyle;
	}
	
	public static CellStyle getNoRecordFoundRowStyleWithoutBorder(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle = workbook.createCellStyle();
		Font hSSFFont = workbook.createFont();
		hSSFFont.setFontName(XSSFFont.DEFAULT_FONT_NAME);
		hSSFFont.setFontHeightInPoints((short) 11);
		hSSFFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		hSSFFont.setColor(IndexedColors.BLACK.index);
		cellStyle.setFont(hSSFFont);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		return cellStyle;
	}
}
