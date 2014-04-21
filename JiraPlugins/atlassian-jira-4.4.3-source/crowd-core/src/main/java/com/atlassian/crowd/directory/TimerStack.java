package com.atlassian.crowd.directory;

import java.text.MessageFormat;
import java.util.Stack;

/**
 * A stack used for timing log messages
 */
public class TimerStack
{
    // the stack of timer information
    private static ThreadLocal<Stack<TimerStackNode>> current = new ThreadLocal<Stack<TimerStackNode>>();

    public static String pop(String message)
    {
        Stack<TimerStackNode> stack = current.get();
        TimerStackNode timerStackNode = stack.pop();
        String formattedTime = formatTime(System.currentTimeMillis() - timerStackNode.startTime);
        String result = null;
        try
        {
            result = new MessageFormat(message).format(new Object[]{formattedTime});
        }
        catch (IllegalArgumentException e)
        {
            result = "IllegalArgumentException: "+e.getMessage()+": "+message;
        }
        if(stack.isEmpty())
            current.remove();
        return result;
    }

    private static String formatTime(long time)
    {
        return String.valueOf(time)+"ms";
    }

    private static class TimerStackNode
    {
        public final long startTime;
        public TimerStackNode()
        {
            this.startTime = System.currentTimeMillis();
        }
    }

    public static void push()
    {
        Stack<TimerStackNode> stack = current.get();
        if(stack==null)
            stack = new Stack<TimerStackNode>();
        stack.push(new TimerStackNode());
        current.set(stack);
    }
}
