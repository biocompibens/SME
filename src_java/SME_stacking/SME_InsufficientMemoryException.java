package ij.plugin.filter.SME_PROJECTION_SRC;

/**
 * Exception thrown when insufficient memory is available to
 * perform an operation.  Designed to be throw before doing 
 * something that would cause a <code>java.lang.OutOfMemoryError</code>.
 */
public class SME_InsufficientMemoryException extends Exception {

    /**
     * Constructor.
     * 
     * @param message an explanatory message.
     */
    public SME_InsufficientMemoryException(String message) {
        super(message);
    }
    
    /**
     * Default constructor.
     */
    public SME_InsufficientMemoryException() {}
    
}
