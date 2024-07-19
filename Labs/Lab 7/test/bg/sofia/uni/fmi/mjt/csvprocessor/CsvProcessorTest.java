package bg.sofia.uni.fmi.mjt.csvprocessor;

import bg.sofia.uni.fmi.mjt.csvprocessor.exceptions.CsvDataNotCorrectException;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.BaseTable;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.ColumnAlignment;
import org.junit.jupiter.api.Test;

import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvProcessorTest {




    @Test
    void testReadCsv() {

        BaseTable table = new BaseTable();
        CsvProcessor csvProcessor = new CsvProcessor(table);

        String csv = """
                name,age,city
                Ivan,12,Sofia
                Gosho,32,Plovdiv
                Pesho,22,Varna
                """;

        try {
            Reader reader = new StringReader(csv);
            csvProcessor.readCsv(reader, ",");
            csvProcessor.writeTable(new OutputStreamWriter(System.out), ColumnAlignment.LEFT, ColumnAlignment.RIGHT, ColumnAlignment.CENTER);
            assertIterableEquals(List.of("name", "age", "city"), table.getColumnNames());
        } catch (CsvDataNotCorrectException e) {
            fail("Should not throw exception");
        }

    }

    @Test
    void testReadCsvWithDuplicateColumnNames() {
        BaseTable table = new BaseTable();
        CsvProcessor csvProcessor = new CsvProcessor(table);

        String csv = """
                name,age,city,name
                Ivan,12,Sofia,Ivan
                Gosho,32,Plovdiv,Gosho
                Pesho,22,Varna,Pesho
                """;

        assertThrows(CsvDataNotCorrectException.class, () -> {
            Reader reader = new StringReader(csv);
            csvProcessor.readCsv(reader, ",");
        });
    }

    @Test
    void testReadCsvWithDifferentRowLengths() {
        BaseTable table = new BaseTable();
        CsvProcessor csvProcessor = new CsvProcessor(table);

        String csv = """
                name,age,city
                Ivan,12,Sofia
                Gosho,32,Plovdiv
                Pesho,22,Varna,extra
                """;

        assertThrows(CsvDataNotCorrectException.class, () -> {
            Reader reader = new StringReader(csv);
            csvProcessor.readCsv(reader, ",");
        });
    }

}
