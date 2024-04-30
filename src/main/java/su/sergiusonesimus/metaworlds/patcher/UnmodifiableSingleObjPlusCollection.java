package su.sergiusonesimus.metaworlds.patcher;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

public class UnmodifiableSingleObjPlusCollection<E extends Object> extends AbstractCollection<E> {

    private E mSingleObj;
    private Collection<E> mPlusCollection;

    public UnmodifiableSingleObjPlusCollection(E singleObject, Collection<E> plusCollection) {
        this.mSingleObj = singleObject;
        this.mPlusCollection = plusCollection;
    }

    public int size() {
        return this.mPlusCollection.size() + 1;
    }

    public Iterator<E> iterator() {
        return new UnmodifiableSingleObjPlusCollection.SingleObjPlusCollIterator(this);
    }

    public class SingleObjPlusCollIterator<T extends Object> implements Iterator<T> {

        T singleObj;
        Iterator<T> curIter;
        boolean isAtZero = true;

        public SingleObjPlusCollIterator(UnmodifiableSingleObjPlusCollection parentCollection) {
            this.singleObj = (T) parentCollection.mSingleObj;
            this.curIter = parentCollection.mPlusCollection.iterator();
        }

        public boolean hasNext() {
            return this.isAtZero || this.curIter.hasNext();
        }

        public T next() {
            if (this.isAtZero) {
                this.isAtZero = false;
                return this.singleObj;
            } else {
                return this.curIter.next();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
