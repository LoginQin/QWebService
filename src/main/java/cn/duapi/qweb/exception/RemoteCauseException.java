package cn.duapi.qweb.exception;

/**
 * Remote Service return an Exception
 * 
 * @author qinwei
 * 
 */
public class RemoteCauseException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 5975584652812432920L;

    String remoteExceptionClass;

    public RemoteCauseException(String msg, Throwable tx) {
        super(msg, tx);
    }

    public RemoteCauseException(String msg, String remoteExceptionClass) {
        super(msg);
        this.remoteExceptionClass = remoteExceptionClass;
    }

    public String getRemoteExceptionClass() {
        return remoteExceptionClass;
    }

    public void setRemoteExceptionClass(String remoteExceptionClass) {
        this.remoteExceptionClass = remoteExceptionClass;
    }
}
