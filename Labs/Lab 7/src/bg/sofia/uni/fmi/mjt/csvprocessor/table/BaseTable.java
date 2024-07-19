package bg.sofia.uni.fmi.mjt.csvprocessor.table;

import bg.sofia.uni.fmi.mjt.csvprocessor.exceptions.CsvDataNotCorrectException;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.column.BaseColumn;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.column.Column;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BaseTable implements Table{

    Collection<BaseColumn> columns;

    @Override
    public void addData(String[] data) throws CsvDataNotCorrectException {
        if(data == null){
            throw new IllegalArgumentException("Data cannot be null");
        }
        if(columns == null){
            columns = new ArrayList<>();
            for(int i = 0; i < data.length; i++){
                columns.add(new BaseColumn());
            }
        }
        if(data.length != columns.size()){
            throw new CsvDataNotCorrectException("Data length is not correct");
        }
        int i = 0;
        for(BaseColumn column : columns){
            column.addData(data[i++]);
        }
    }

    @Override
    public Collection<String> getColumnNames() {
        List<String> names = new ArrayList<>();
        for(BaseColumn column : columns){
            names.add(column.getData().iterator().next());
        }
        return List.copyOf(names);
    }

    @Override
    public Collection<String> getColumnData(String column) {
        if(column == null || column.isBlank()){
            throw new IllegalArgumentException("Column cannot be null or blank");
        }
        return getColumnByName(column).getData();
    }

    private Column getColumnByName(String column){
        for(Column col : columns){
            if(col.getData().iterator().next().equals(column)){
                return col;
            }
        }
        throw new IllegalArgumentException("No column with name " + column + " found");
    }

    @Override
    public int getRowsCount() {
        return columns.iterator().next().getData().size();
    }


}
