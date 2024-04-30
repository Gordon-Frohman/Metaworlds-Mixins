package su.sergiusonesimus.metaworlds.core;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

public class CompositeUnmodifiableCollection<E extends Object> extends AbstractCollection<E> {

    private final Collection<E> collection1;
    private final Collection<E> collection2;

    public CompositeUnmodifiableCollection(Collection<E> list1, Collection<E> list2) {
        this.collection1 = list1;
        this.collection2 = list2;
    }

    public int size() {
        return this.collection1.size() + this.collection2.size();
    }

    public Iterator<E> iterator() {
        return new CompositeUnmodifiableCollection.CUCIterator(this);
    }

    public class CUCIterator<T extends Object> implements Iterator<T> {

        Iterator<T> iterFirst;
        Iterator<T> iterSecond;
        Iterator<T> curIter;

        public CUCIterator(CompositeUnmodifiableCollection parentCollection) {
            this.iterFirst = parentCollection.collection1.iterator();
            this.iterSecond = parentCollection.collection2.iterator();
            this.curIter = this.iterFirst;
        }

        public boolean hasNext() {
            return this.iterFirst.hasNext() || this.iterSecond.hasNext();
        }

        public T next() {
            return this.iterFirst.hasNext() ? this.iterFirst.next() : this.iterSecond.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
