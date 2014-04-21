package com.atlassian.jira.issue.attachment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

/**
 * Copied from FECRU
 * <p/>
 * ./java/com/cenqua/fisheye/Path.java r54145
 */
public class Path implements Serializable, Comparable<Path>
{

    private static final Pattern SPLIT_PATTERN = Pattern.compile("/");
    private static final Pattern SLOSH_SPLIT_PATTERN = Pattern.compile("[/|\\\\]");

    private static final Map<String, String> componentCache = new WeakHashMap<String, String>();

    private final String[] path;
    private String stringRep = null;


    private Path(String[] comp)
    {
        path = canonicalize(comp);
    }

    public Path()
    {
        path = new String[0];
    }

    public Path(CharSequence path)
    {
        this.path = canonicalize(split(path));
    }

    public Path(CharSequence path, boolean allowSloshes)
    {
        this.path = canonicalize(split(path, allowSloshes));
    }

    public Path(Path path)
    {
        this.path = path.getComponents();
    }

    public Path(Path aParent, String aPath)
    {
        path = canonicalize(join(aParent.getComponents(), split(aPath)));
    }

    public Path(Path aParent, Path aPath)
    {
        path = canonicalize(join(aParent.getComponents(), aPath.getComponents()));
    }

    public Path(Path aParent, String aPath, boolean allowSloshes)
    {
        path = canonicalize(join(aParent.getComponents(), split(aPath, allowSloshes)));
    }

    public Path(String aParent, Path aPath)
    {
        path = canonicalize(join(split(aParent), aPath.getComponents()));
    }

    private String[] canonicalize(String[] components)
    {
        for (int i = 0; i < components.length; i++)
        {
            String component = components[i];
            String canonical = componentCache.get(component);
            if (canonical == null)
            {
                synchronized (componentCache)
                {
                    // construct new String so that it is weakly referenced
                    // noinspection RedundantStringConstructorCall
                    componentCache.put(new String(component), component);
                }
            }
            else
            {
                components[i] = canonical;
            }
        }
        return components;
    }

    public Path(List<String> start)
    {
        this(start.toArray(new String[start.size()]));
    }

    public Path getParent()
    {
        if (path.length == 0)
        {
            return this;
        }
        String[] comp = new String[path.length - 1];
        System.arraycopy(path, 0, comp, 0, comp.length);
        return new Path(comp);
    }

    /**
     * @return the string representation of the path
     */
    public String getPath()
    {
        if (stringRep == null)
        {
            StringBuilder buf = new StringBuilder();
            String sep = "";
            for (String path : this.path)
            {
                buf.append(sep).append(path);
                sep = "/";
            }
            stringRep = buf.toString();
        }
        return stringRep;
    }


    public String[] getComponents()
    {
        return path;
    }

    public Path[] getPathComponents()
    {
        Path[] paths = new Path[path.length + 1];
        for (int i = 0; i <= path.length; i++)
        {
            String[] comp = new String[i];
            System.arraycopy(path, 0, comp, 0, i);
            paths[i] = new Path(comp);
        }
        return paths;
    }

    public boolean isAncestor(Path descendant)
    {
        if (descendant.equals(this))
        {
            return false;
        }
        Path commonRoot = getCommonRoot(descendant);
        return commonRoot.equals(this);
    }

    public Path getCommonRoot(Path other)
    {
        int limit = Math.min(path.length, other.path.length);
        int i = 0;
        while (i < limit && path[i].equals(other.path[i]))
        {
            ++i;
        }

        if (i == 0)
        {
            return new Path("/");
        }
        else
        {
            String[] commonComponents = new String[i];
            System.arraycopy(path, 0, commonComponents, 0, i);
            return new Path(commonComponents);
        }
    }

    /**
     * if other is an exact tail of this, return the path leading up to that tail otherwise return null
     */
    public Path getStripTail(Path other)
    {
        if (path.length < other.path.length)
        {
            return null;
        }
        for (int i = 0; i < other.path.length; i++)
        {
            if (!path[path.length - i - 1].equals(other.path[other.path.length - i - 1]))
            {
                return null;
            }
        }
        return getSubPath(path.length - other.path.length);
    }

    /**
     * returns a path relative to aStartPath. If the paths have no common prefix, then null object is returned.
     * <p/>
     * TODO revisit this method, it's meaning and where it is used
     */
    public Path getRelativePath(Path aStartPath)
    {
        return getRelativePath(aStartPath, true);
    }

    public Path getRelativePath(Path aStartPath, boolean caseSensitive)
    {
        List<String> start = simplify(aStartPath.path);
        if (start.isEmpty())
        {
            // relative to root
            return this;
        }

        List<String> us = simplify(path);
        // remove common prefixes
        while ((start.size() > 0) && (us.size() > 0) && isEqualFirstComponent(start, us, caseSensitive))
        {
            start.remove(0);
            us.remove(0);
        }

        int nback = start.size();
        for (int i = 0; i < nback; i++)
        {
            start.add("..");
            start.add("..");
        }
        start.addAll(us);
        return new Path(start).simplify();
    }

    private boolean isEqualFirstComponent(List<String> start, List<String> us, boolean caseSensitive)
    {
        String lhs = start.get(0);
        String rhs = us.get(0);
        if (!caseSensitive)
        {
            lhs = lhs.toLowerCase(Locale.US);
            rhs = rhs.toLowerCase(Locale.US);
        }
        return lhs.equals(rhs);
    }

    public String getName()
    {
        if (path.length == 0)
        {
            return ""; // is this right?
        }
        return path[path.length - 1];
    }

    public String toString()
    {
        return getPath();
    }

    public Path simplify()
    {
        final List<String> list = simplify(path);
        return new Path(list.toArray(new String[list.size()]));
    }

    /**
     * Indicates this path points to a locatio above the notional root.
     *
     * @return true if the path is above the root.
     */
    public boolean isAbove()
    {
        return (path.length > 0) && (path[0].equals(".."));
    }

    public boolean equals(Object o)
    {
        // CHECKSTYLE:OFF
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Path))
        {
            return false;
        }

        final Path path = (Path) o;

        return Arrays.equals(this.path, path.path);
        // CHECKSTYLE:ON
    }

    public int hashCode()
    {
        int result = 0;
        for (String s : path)
        {
            result += s.hashCode();
        }
        return result;
    }

    private static List<String> simplify(String[] aPath)
    {
        List<String> path = new ArrayList<String>(Arrays.asList(aPath));
        for (int i = 0; i < path.size(); i++)
        {
            String s1 = path.get(i);
            if ("..".equals(s1) && (i > 0))
            {
                String s0 = path.get(i - 1);
                if (!"..".equals(s0))
                {
                    path.remove(i--); // remove ".."
                    path.remove(i--); // remove previous element
                }
            }
            else if (".".equals(s1))
            {
                path.remove(i--);
            }
        }
        if ((path.size() == 1))
        {
            // turn [""] into []
            String s0 = path.get(0);
            if (s0.length() == 0)
            {
                path.remove(0);
            }
        }
        return path;
    }

    private static String[] join(String[] aLeft, String[] aRight)
    {
        String[] result = new String[aLeft.length + aRight.length];
        System.arraycopy(aLeft, 0, result, 0, aLeft.length);
        System.arraycopy(aRight, 0, result, aLeft.length, aRight.length);
        return result;
    }

    /**
     * Intelligently Put a "/" between two strings.
     */
    public static String join(String a, String b)
    {
        if (a == null)
        {
            a = "";
        }

        if (b == null)
        {
            b = "";
        }

        if (a.length() == 0)
        {
            return b;
        }

        if (b.length() == 0)
        {
            return a;
        }

        if (a.endsWith("/"))
        {
            a = a.substring(0, a.length() - 1);
        }

        if (b.startsWith("/"))
        {
            b = b.substring(1);
        }

        return a + "/" + b;
    }

    public static String[] split(CharSequence aPath)
    {
        return split(aPath, false);
    }

    public static String[] split(CharSequence aPath, boolean allowSloshes)
    {
        if ((aPath == null) || aPath.length() == 0)
        {
            return new String[0];
        }
        if ((aPath.length() > 0 && (aPath.charAt(0) == '/')))
        {
            aPath = aPath.subSequence(1, aPath.length()); // the split function includes an extra match, otherwise
        }
        if (aPath.length() == 0)
        {
            return new String[0];
        }
        Pattern splitPattern = allowSloshes ? SLOSH_SPLIT_PATTERN : SPLIT_PATTERN;
        return splitPattern.split(aPath);
    }

    public boolean isRoot()
    {
        return path.length == 0 || ((path.length == 1) && (path[0].length() == 0));
    }

    public Path trimFirst()
    {
        return trimFirst(1);
    }

    /**
     * @param n number of components to trim
     */
    public Path trimFirst(int n)
    {
        if (path.length == 0)
        {
            return this; // we are already empty
        }

        int newLen = path.length - n;
        if (newLen <= 0)
        {
            return new Path();
        }
        String[] p = new String[newLen];
        System.arraycopy(path, n, p, 0, p.length);
        return new Path(p);
    }

    public Path trimLast()
    {
        if (path.length == 0)
        {
            return this;
        }
        if (path.length == 1)
        {
            return new Path();
        }
        String[] p = new String[path.length - 1];
        System.arraycopy(path, 0, p, 0, p.length);
        return new Path(p);
    }

    public String getComponent(int i)
    {
        return path[i];
    }

    public int numComponents()
    {
        return path.length;
    }

    public int getNumComponents()
    {
        return numComponents();
    }

    public int compareTo(Path o)
    {
        if (o == null)
        {
            return -1;
        }

        String[] path = o.path;
        int i;
        for (i = 0; (i < this.path.length) && (i < path.length); i++)
        {
            String a = this.path[i];
            String b = path[i];
            int r = a.compareTo(b);
            if (r != 0)
            {
                return r;
            }
        }
        // they have a common prefix up to i (and i is the length of one of them)
        if (this.path.length < path.length)
        {
            return -1;
        }
        if (path.length < this.path.length)
        {
            return 1;
        }
        return 0;

    }

    public static String parseName(String p)
    {
        int idx = p.lastIndexOf("/");
        if (idx == -1)
        {
            return p;
        }
        return p.substring(idx + 1);
    }

    public static String parseDir(String p)
    {
        int idx = p.lastIndexOf("/");
        if (idx == -1)
        {
            return "";
        }
        return p.substring(0, idx);
    }

    /**
     * abbreviate a path by removing path components from the middle until the length of the path (as via .getPath()) is
     * not greater then maxLength.
     * <p/>
     * The first and last path component are always in the returned path.
     * <p/>
     * If any path components were removed, an ellippses "..." will appear in the middle
     * <p/>
     * The returned path may in some cases still be greater.
     */
    public Path abbreviate(int maxLength)
    {
        return abbreviateImpl(maxLength).path;
    }

    /**
     * Returns a similar result to {@link #getPathComponents()}, but abbreviates the result (inserts null at the break
     * point)
     */
    public Path[] abbreviatePathComponents(int maxLength)
    {
        List<Path> pc = abbreviateImpl(maxLength).pathComponents;
        return pc.toArray(new Path[pc.size()]);
    }

    private static class AbbrevResult
    {
        final Path path;
        final List<Path> pathComponents;

        public AbbrevResult(Path path, List<Path> pathComponents)
        {
            this.path = path;
            this.pathComponents = pathComponents;
        }
    }

    private AbbrevResult abbreviateImpl(int maxLength)
    {
        final String ellipses = "...";

        if (path.length <= 2)
        {
            return new AbbrevResult(this, Arrays.asList(getPathComponents()));
        }

        int len = getPath().length();
        if (len <= maxLength)
        {
            return new AbbrevResult(this, Arrays.asList(getPathComponents()));
        }

        ArrayList<String> components = new ArrayList<String>(Arrays.asList(path));
        ArrayList<Path> pathComponents = new ArrayList<Path>(Arrays.asList(getPathComponents()));

        int insertEllipsesAt = components.size() / 2;
        while (len > maxLength)
        {
            if (components.size() <= 2)
            {
                // always leave one at either end
                break;
            }
            int midPoint = components.size() / 2;
            insertEllipsesAt = Math.min(insertEllipsesAt, midPoint);
            String s = components.remove(midPoint);
            pathComponents.remove(midPoint);
            len -= s.length() + 1;
        }

        components.add(insertEllipsesAt, ellipses); // ellipses
        pathComponents.add(insertEllipsesAt, null);

        return new AbbrevResult(new Path(components), pathComponents);

    }

    /**
     * determines if this path begins with all of <code>p</code>
     */
    public boolean hasPrefix(Path p)
    {
        return hasPrefix(p, true);
    }

    public boolean hasPrefix(Path p, boolean caseSensitive)
    {
        if (p.numComponents() > numComponents())
        {
            return false; // p is too long
        }

        for (int i = 0; i < p.path.length; i++)
        {
            String p1 = p.path[i];
            String p0 = path[i];
            if (!caseSensitive)
            {
                p1 = p1.toLowerCase(Locale.US);
                p0 = p0.toLowerCase(Locale.US);
            }
            if (!p1.equals(p0))
            {
                return false;
            }
        }
        return true;
    }

    public Path normalize()
    {
        List<String> components = new ArrayList<String>(Arrays.asList(path));
        for (int i = 0; i < components.size();)
        {
            String component = components.get(i);
            if (component.equals("."))
            {
                components.remove(i);
            }
            else if (component.equals("..") && i != 0)
            {
                String previous = components.get(i - 1);
                if (!previous.equals(".."))
                {
                    components.remove(i);
                    components.remove(i - 1);
                    i--;
                }
                else
                {
                    i++;
                }
            }
            else
            {
                i++;
            }
        }

        return new Path(components.toArray(new String[components.size()]));
    }

    public String getPath(boolean caseSensitive)
    {
        String normalized;
        if (caseSensitive)
        {
            normalized = getPath();
        }
        else
        {
            normalized = getPath().toLowerCase(Locale.US);
        }
        return normalized;
    }

    public void toLowerCase()
    {
        for (int i = 0; i < path.length; i++)
        {
            path[i] = path[i].toLowerCase();
        }
    }

    public Path getSubPath(int depth)
    {
        depth = Math.min(depth, path.length);
        String[] comps = new String[depth];
        System.arraycopy(path, 0, comps, 0, depth);
        return new Path(comps);
    }

    public Path getPathHead()
    {
        if (path.length == 0)
        {
            return this;
        }
        return new Path(path[0]);
    }

    /**
     * @return a String composed of all the path segments after the first segment
     */
    public String getTail()
    {
        return trimFirst(1).getPath();
    }

    public Path getTailPath()
    {
        return trimFirst(1);
    }

    /**
     * @return the file extension of the path (with no .), or null if there is no file extension
     */
    public String getExtension()
    {
        String extn = null;
        if (path.length > 0)
        {
            String file = getComponent(path.length - 1);
            int dot = file.lastIndexOf(".");
            if (dot != -1 && dot != file.length() - 1)
            {
                extn = file.substring(dot + 1);
            }
        }
        return extn;
    }
}
