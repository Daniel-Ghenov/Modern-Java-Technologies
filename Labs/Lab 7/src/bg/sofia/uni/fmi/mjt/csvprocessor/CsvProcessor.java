package bg.sofia.uni.fmi.mjt.csvprocessor;

import bg.sofia.uni.fmi.mjt.csvprocessor.exceptions.CsvDataNotCorrectException;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.BaseTable;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.Table;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.ColumnAlignment;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.MarkdownTablePrinter;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.TablePrinter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class CsvProcessor implements CsvProcessorAPI {

    private final Table table;
    private final TablePrinter tablePrinter;
    public CsvProcessor() {
        this(new BaseTable());
    }

    public CsvProcessor(Table table) {
        this.table = table;
        this.tablePrinter = new MarkdownTablePrinter();
    }

    @Override
    public void readCsv(Reader reader, String delimiter) throws CsvDataNotCorrectException {

        try(BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = bufferedReader.readLine();

            while(line != null){
                String[] data = line.split("\\Q" + delimiter + "\\E");
                table.addData(data);
                line = bufferedReader.readLine();
            }
        }catch (IOException e){
            throw new CsvDataNotCorrectException("Error reading from reader");
        }

        Set<String> names = new TreeSet<>(table.getColumnNames());
        if(names.size() != table.getColumnNames().size()){
            throw new CsvDataNotCorrectException("Duplicate column names");
        }
    }

    @Override
    public void writeTable(Writer writer, ColumnAlignment... alignments) {
        Collection<String> lines = tablePrinter.printTable(table, alignments);

        try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            for (int i = 0; i < lines.size(); i++) {
                bufferedWriter.write(lines.toArray()[i].toString());
                if(i != lines.size() - 1){
                    bufferedWriter.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing to writer");
        }

    }
}
