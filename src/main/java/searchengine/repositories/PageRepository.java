package searchengine.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {
 //   List<Page> findAllBySiteModel(SiteModel siteModel);
//    Optional<Page> findPageByPath(String path);
}
