package org.talend.esb.sam.agent.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WireTapInputStream extends InputStream {
    private InputStream origStream;
    private OutputStream os;
    
    public WireTapInputStream(InputStream origStream, OutputStream os) {
        this.origStream = origStream;
        this.os = os;
    }

    @Override
    public int read() throws IOException {
        int c = origStream.read();
        if (c!=-1) {
            os.write(c);
        } else {
            os.close();
        }
        return c;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int count = origStream.read(b);
        os.write(b, 0, count);
        return count;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = origStream.read(b, off, len);
        os.write(b, off, count);
        return count;
    }

    @Override
    public long skip(long n) throws IOException {
        return origStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return origStream.available();
    }

    @Override
    public void close() throws IOException {
        os.close();
        origStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new RuntimeException("Reset not implemented");
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new RuntimeException("Reset not implemented");
    }

    @Override
    public boolean markSupported() {
        return false;
    }
    
}
