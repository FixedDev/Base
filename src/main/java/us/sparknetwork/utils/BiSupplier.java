package us.sparknetwork.utils;

/**
 * Represents a supplier of 2 types of results.
 *
 * <p>There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 *
 * @param <T> the first type of results supplied by this supplier
 * @param <T1> the second type of result supplied by this supplier
 * @since 1.8
 */
public interface BiSupplier<T, T1> {

    /**
     * Gets the first result.
     *
     * @return a result
     */
    T getFirst();

    /**
     * Gets the second result.
     *
     * @return a result
     */
    T1 getSecond();
}
