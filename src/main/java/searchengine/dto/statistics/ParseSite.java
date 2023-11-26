package searchengine.dto.statistics;

import lombok.Data;
import lombok.NoArgsConstructor;
import searchengine.model.SiteModel;
import searchengine.model.Status;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

@Data
public class ParseSite implements Runnable {

    SiteModel siteModel;
    ForkJoinPool pool;

    public ParseSite(SiteModel siteModel) {
        this.siteModel = siteModel;
    }

    @Override
    public void run() {
        //ParseLinksTask parseLinksTask = new ParseLinksTask(siteModel.getUrl(), siteModel);
        //pool.invoke(parseLinksTask);

    }
}
