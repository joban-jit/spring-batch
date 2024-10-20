package com.springbatch.listener;

import com.springbatch.records.OrdersRecord;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class OrdersDataSkipListener implements SkipListener<OrdersRecord, OrdersRecord> {
    Path skippedItemsFile;

    public OrdersDataSkipListener(String skippedItemsFile){
        this.skippedItemsFile = Paths.get(skippedItemsFile);
    }


    @Override
    public void onSkipInRead(Throwable t) {
        if(t instanceof FlatFileParseException exception){
            String rawLine = exception.getInput();
            int lineNumber = exception.getLineNumber();
            String skippedLine  = lineNumber+"|"+rawLine+System.lineSeparator();
            try{
                Files.writeString(this.skippedItemsFile, skippedLine, StandardOpenOption.APPEND, StandardOpenOption.CREATE);

            } catch (IOException e) {
                throw new RuntimeException("Unable to write skipped item "+skippedLine);
            }
        }
    }
}
