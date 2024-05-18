package indigo.clouddrive.frontend.helpers;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class PathHelpers {
    public static String getPathEnding(String path){
//        String[] split = path.split("/");
//        if(split.length == 0){
//            return "./";
//        }
//        return split[split.length - 1];
        try {
            Path pathInstance = Paths.get(path);
            return pathInstance.getFileName().toString();
        }
        catch(InvalidPathException ex){
            return "";
        }
    }

    public static String getPreviousPathName(String path){
        String[] split = path.split("/");
        if(split.length == 0 || split.length == 1){
            return "";
        }
        String[] adjustedSplit = Arrays.copyOf(split, split.length-1);
        String finalPath = String.join("/", adjustedSplit);
        if(finalPath.equals(".")){
            return "";
        }
        return finalPath;
    }

    public static String getSubPathWithoutStart(String base, String toRemove){
        return base.replace(toRemove, "");
    }

    public static String getPathWithoutStartName(String path) {
        String[] split = path.split("/");
        if(split.length == 0 || split.length == 1){
            return path;
        }
        String[] adjustedSplit = Arrays.copyOfRange(split, 1, split.length);
        return String.join("/", adjustedSplit);
    }

    public static String getTrimmedPath(String path){
        //Regex, který odstaní všechny počáteční, koncové a mezicesty typu ./
        return path.replaceAll("(^/*\\.(/)*)|(/\\.(?=/|$))|((/)+$)", "").replaceAll("^/", "");
    }
}
