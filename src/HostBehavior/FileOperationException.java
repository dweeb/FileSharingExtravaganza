package HostBehavior;

public class FileOperationException extends Throwable {
    /**
     *      This is used to catch IOExceptions related to files only, so that only IOExceptions related to connectivity
     *      are thrown all the way up outside the connection loop, which in turn closes the connection itself
     */
}
