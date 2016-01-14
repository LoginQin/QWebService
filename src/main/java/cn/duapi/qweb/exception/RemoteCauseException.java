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

    public RemoteCauseException(String msg, Throwable tx) {
        super(msg, tx);
    }
}
