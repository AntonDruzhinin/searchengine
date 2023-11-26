package searchengine.dto.statistics;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

//Класс из раздела мноргопоточность
//Рассмотреть варианты внедрения
public class MapCreator extends RecursiveAction {

     private static Map<String, String> parentLinkMap;
     public static LinkedList<String> linkedList = new LinkedList<>();
     private final String url;
     private final int level;

     public MapCreator(Map<String, String> parentLinkMap) {
         MapCreator.parentLinkMap = parentLinkMap;
         this.url = parentLinkMap.entrySet()
                 .stream()
                 .filter(entry -> entry.getValue().equals("null"))
                 .findFirst()
                 .get()
                 .getKey();
         this.level = 0;
     }

     private MapCreator(String url, int level) {
         this.url = url;
         this.level = level;
     }

     private String urlWithTabs() {
         String link = this.url;
         for (int t = 0; t < this.level; t++) {
             link = "\t".concat(link);
         }
         return link;
     }


     @Override
     protected void compute() {
         linkedList.add(urlWithTabs());
         Set<String> set = parentLinkMap
                 .entrySet()
                 .stream()
                 .filter(e -> e.getValue().equals(url))
                 .map(Map.Entry::getKey)
                 .collect(Collectors.toSet());
         if (!set.isEmpty()) {
             for (String currentLink : set) {
                 new MapCreator(currentLink, this.level + 1).invoke();
             }
         }
     }
 }

