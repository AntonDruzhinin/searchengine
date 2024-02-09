package searchengine.dto.statistics;

import lombok.Getter;
import searchengine.model.Page;
import searchengine.model.SiteModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListPages {
    private SiteModel siteModel;

    public ListPages(SiteModel siteModel) {
        this.siteModel = siteModel;
    }

    @Getter
    private List<Page> pagesList= new CopyOnWriteArrayList<>();
    public synchronized void addPage(Page page){
        pagesList.add(page);
        siteModel.setStatusTime(LocalDateTime.now());
    }

}
