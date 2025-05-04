package it.paolinucz.lazy.orm.repository;

import it.paolinucz.lazy.orm.config.LazyJPAConfig;
import it.paolinucz.lazy.orm.exceptions.LazyNotFoundException;
import it.paolinucz.lazy.orm.model.LazyModel;
import it.paolinucz.lazy.orm.pagination.LazyPagination;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

public abstract class LazyRepository<M extends LazyModel, K> {

    private final Class<M> modelClass;
    private static EntityManagerFactory entityManagerFactory = LazyJPAConfig.entityManagerFactory();


    @SuppressWarnings("unchecked")
    protected LazyRepository() {
        this.modelClass = (Class<M>) ((java.lang.reflect.ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected Class<M> getModelClass() {
        return modelClass;
    }

    public void persist(M entity) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            entityManager.getTransaction().begin();
            entityManager.persist(entity);
            entityManager.getTransaction().commit();
        }
    }

    public List<M> findAll() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            final String query = "select m from " + modelClass.getSimpleName() + " m";
            return entityManager.createQuery(query, getModelClass()).getResultList();
        }
    }

    public Optional<M> findById(K id) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return Optional.ofNullable(entityManager.find(modelClass, id));
        }
    }

    public void deleteById(K id) throws LazyNotFoundException {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            final M object = this.findById(id)
                    .orElseThrow(LazyNotFoundException::new);
            entityManager.getTransaction().begin();
            entityManager.remove(object);
            entityManager.getTransaction().commit();
        }
    }

    public List<M> dynamicSearch(M criteria) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            List<LazyModel.ObjectInfo> persistenceInfo = criteria.extractPersistenceInfo();

            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<M> query = builder.createQuery(modelClass);
            Root<M> root = query.from(modelClass);

            List<Predicate> predicates = persistenceInfo
                    .stream()
                    .map(info -> builder.equal(root.get(info.getAttributeName()), info.getValue()))
                    .toList();

            if (!predicates.isEmpty()) {
                query.select(root).where(predicates.toArray(new Predicate[0]));
            } else {
                query.select(root);
            }

            TypedQuery<M> typedQuery = entityManager.createQuery(query);
            return typedQuery.getResultList();
        }
    }

    public LazyPagination<M> dynamicSearch(M criteria, int page, int size) {
        return toPagination(dynamicSearch(criteria), page, size);
    }

    public LazyPagination<M> findAll(int page, int size) {
        return toPagination(findAll(), page, size);
    }

    private LazyPagination<M> toPagination(List<M> data, int page, int size) {
        final int fromIndex = Math.min(page * size, data.size());
        final int toIndex = Math.min(fromIndex + size, data.size());

        return LazyPagination
                .<M>builder()
                .content(data.subList(fromIndex, toIndex))
                .page(page)
                .size(size)
                .totalCount(data.size())
                .build();
    }


}