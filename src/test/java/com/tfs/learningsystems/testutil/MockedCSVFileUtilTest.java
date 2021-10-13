package com.tfs.learningsystems.testutil;

import static org.mockito.Mockito.when;

import com.opencsv.CSVReader;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.util.CSVFileUtil;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import javax.ws.rs.BadRequestException;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest(CSVFileUtil.class)
public class MockedCSVFileUtilTest {

  @Autowired
  private AppConfig appConfig;


  @Test(expected = BadRequestException.class)
  public void testValidateColumnsInvalidCSVFileException() throws Exception {
    FileInputStream mockedFileInputStream = Mockito.mock(FileInputStream.class);
    PowerMockito.whenNew(FileInputStream.class).withAnyArguments()
        .thenReturn(mockedFileInputStream);

    InputStreamReader mockedInputStreamReader = Mockito.mock(InputStreamReader.class);
    PowerMockito.whenNew(InputStreamReader.class).withAnyArguments()
        .thenReturn(mockedInputStreamReader);

    CSVReader mockedReader = Mockito.mock(CSVReader.class);
    when(mockedReader.readNext()).thenReturn(new String[]{"filename", "column1", "column2"});
    PowerMockito.whenNew(CSVReader.class).withAnyArguments().thenReturn(mockedReader);

    List<String> requiredColumns = new ArrayList<>();
    requiredColumns.add("filename");
    requiredColumns.add("transcription");

    CSVFileUtil.validate("/invalid/path", requiredColumns);


  }

  @Test
  public void testValidateColumnsValidColumns() throws Exception {
    FileInputStream mockedFileInputStream = Mockito.mock(FileInputStream.class);
    PowerMockito.whenNew(FileInputStream.class).withAnyArguments()
        .thenReturn(mockedFileInputStream);

    InputStreamReader mockedInputStreamReader = Mockito.mock(InputStreamReader.class);
    PowerMockito.whenNew(InputStreamReader.class).withAnyArguments()
        .thenReturn(mockedInputStreamReader);

    CSVReader mockedReader = Mockito.mock(CSVReader.class);
    when(mockedReader.readNext()).thenReturn(new String[]{"filename", "transcription", "columnX"});
    PowerMockito.whenNew(CSVReader.class).withAnyArguments().thenReturn(mockedReader);

    List<String> requiredColumns = new ArrayList<>();
    requiredColumns.add("filename");
    requiredColumns.add("transcription");

    CSVFileUtil.validate("/invalid/path", requiredColumns);
  }

}
