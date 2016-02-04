package cn.duapi.qweb.exception;

public class ClientErrorException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ClientErrorException(String msg) {
        super(msg);
    }

}
