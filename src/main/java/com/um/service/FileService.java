package com.um.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

@Service
public class FileService {

    /**
     * Метод возвращает расширение файла, переданного в качестве параметра
     */
    public String fileExtension(String fileName){
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * В методе выполняется парсинг CSV-файлов
     */
    public void processCSV(String fileName) throws FileNotFoundException {
        List<String[]> lines = null;
        CSVReader reader = new CSVReader(new FileReader(fileName));

        try{
            lines = reader.readAll();
        } catch (IOException e){
            System.out.println("{\"id\":" + "--" + ", \"amount\":" + "--" +
                    ", \"comment\":\"" + "--" + "\", \"filename\":\"" + fileName + "\", \"line\":" +
                    "--" + ", \"result\":" + "\"" + "Exception in CSV File" + "\" }");
        }

        if(lines != null && lines.size() != 0){
            writeLines(lines, fileName);
        }
    }

    /**
     * В методе выполняется парсинг JSON-файлов
     */
    public void processJSON(String fileName) throws FileNotFoundException {
        List<String[]> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        int count = 1;
        String str;

        try{
            while((str = br.readLine()) != null){
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> map;
                map = objectMapper.readValue(str, new TypeReference<Map<String,String>>(){});
                List<String> array = new ArrayList<>();

                if(map != null && map.containsKey("orderId")){
                    array.add(map.get("orderId"));
                }
                if(map != null && map.containsKey("amount")){
                    array.add(map.get("amount"));
                }
                if(map != null && map.containsKey("currency")){
                    array.add(map.get("currency"));
                }
                if(map != null && map.containsKey("comment")){
                    array.add(map.get("comment"));
                }

                lines.add(array.toArray(new String[0]));
                count++;
            }
        } catch (IOException e){
            System.out.println("{\"id\":" + "--" + ", \"amount\":" + "--" +
                    ", \"comment\":\"" + "--" + "\", \"filename\":\"" + fileName + "\", \"line\":" +
                    count + ", \"result\":" + "\"" + "Wrong line format" + "\" }");
        }

        if(lines.size() != 0){
            writeLines(lines, fileName);
        }
    }

    /**
     * В методе выполняется парсинг XLSX-файлов
     */
    public void processXLSX(String fileName) {
        List<String[]> lines = new ArrayList<>();

        try{
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(fileName));
            XSSFSheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                Iterator<Cell> cells = row.iterator();
                List<String> array = new ArrayList<>();
                while (cells.hasNext()) {
                    Cell cell = cells.next();
                    CellType cellTypeEnum = cell.getCellType();
                    if (cellTypeEnum.name().equals("NUMERIC")) {
                        NumberFormat nf = DecimalFormat.getInstance(Locale.ENGLISH);
                        nf.setMinimumFractionDigits(0);
                        double nn = cell.getNumericCellValue();
                        String format = nf.format(nn);
                        array.add(format);
                    } else if (cellTypeEnum.name().equals("STRING")) {
                        array.add(cell.getStringCellValue());
                    }
                }
                lines.add(array.toArray(new String[0]));
            }
        } catch (IOException e){
            System.out.println("{\"id\":" + "--" + ", \"amount\":" + "--" +
                    ", \"comment\":\"" + "--" + "\", \"filename\":\"" + fileName + "\", \"line\":" +
                    "--" + ", \"result\":" + "\"" + "Exception in XLSX File" + "\" }");
        }

        if(lines.size() != 0){
            writeLines(lines, fileName);
        }
    }

    /**
     * Метод выводит результаты парсинга файлов на консоль.
     * Данный метод вызывается в методах processCSV(), processJSON()
     * и processXLSX().
     */
    public static void writeLines(List<String[]> array, String fileName){
        int count = 1;
        StringJoiner sj = new StringJoiner(", ");

        for(String[] item : array){
            if(item.length == 4){
                try{
                    Integer.parseInt(item[0]);
                } catch (Exception e) {
                    sj.add("Order id has wrong format");
                }

                try{
                    Double.parseDouble(item[1]);
                } catch (Exception e) {
                    sj.add("Amount has wrong format");
                }

                System.out.println("{\"id\":" + item[0] + ", \"amount\":" + item[1] +
                        ", \"comment\":\"" + item[3] + "\", \"filename\":\"" + fileName + "\", \"line\":" +
                        count + ", \"result\":" + "\"" + (sj.length() > 0 ? sj.toString() : "OK") + "\" }");

            } else if(item.length > 4) {
                System.out.println("{\"id\":" + "--" + ", \"amount\":" + "--" +
                        ", \"comment\":\"" + "--" + "\", \"filename\":\"" + fileName + "\", \"line\":" +
                        count + ", \"result\":" + "\"" + "Excessive values in line" + "\" }");

            } else {
                System.out.println("{\"id\":" + "--" + ", \"amount\":" + "--" +
                        ", \"comment\":\"" + "--" + "\", \"filename\":\"" + fileName + "\", \"line\":" +
                        count + ", \"result\":" + "\"" + "Missing values in line" + "\" }");
            }

            sj = new StringJoiner(", ");
            count++;
        }
    }
}