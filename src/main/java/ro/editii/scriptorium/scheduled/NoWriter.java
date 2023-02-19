package ro.editii.scriptorium.scheduled;

import java.io.IOException;
import java.io.Writer;

public class NoWriter extends Writer {
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        // noop
    }

    @Override
    public void flush() throws IOException {
        // noop
    }

    @Override
    public void close() throws IOException {
        // noop
    }
}
