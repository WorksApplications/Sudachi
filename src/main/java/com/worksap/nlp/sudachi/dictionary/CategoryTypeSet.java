package com.worksap.nlp.sudachi.dictionary;

import java.util.AbstractSet;
import java.util.Iterator;

public class CategoryTypeSet extends AbstractSet<CategoryType> implements Cloneable {

    private int types;

    CategoryTypeSet() { types = 0; }

    protected CategoryTypeSet(int types) { this.types = types; }

    @Override
    public CategoryTypeSet clone() {
        return new CategoryTypeSet(types);
    }

    @Override
    public int size() { return Integer.bitCount(types); }

    @Override
    public boolean isEmpty() { return types == 0; }

    @Override
    public boolean contains(Object o) {
        CategoryType type = (CategoryType)o;
        return (types & type.getId()) != 0;
    }

    @Override
    public boolean add(CategoryType e) {
        boolean ret = contains(e);
        types |= e.getId();
        return ret;
    }

    @Override
    public boolean remove(Object e) {
        boolean ret = contains(e);
        CategoryType type = (CategoryType)e;
        types &= ~type.getId();
        return ret;
    }

    @Override
    public void clear() { types = 0; }

    class itr implements Iterator<CategoryType> {

        private int remainTypes;
        private int id;
        private CategoryType last;

        itr(int types) { this.remainTypes = types; id = 1; last = null; }
        @Override public boolean hasNext() { return remainTypes != 0; }
        @Override public CategoryType next() {
            if (remainTypes == 0) return null;
            while ((remainTypes & 1) == 0) {
                remainTypes >>>= 1;
                id <<= 1;
            }
            last = CategoryType.getType(id);
            remainTypes >>>= 1;
            id <<= 1;
            return last;
        }
        @Override public void remove() {
            if (last == null) throw new IllegalStateException();
            types &= ~last.getId();
            last = null;
        }
    }

    @Override
    public Iterator<CategoryType> iterator() {
        return new itr(types);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof CategoryTypeSet) {
            return this.types == ((CategoryTypeSet)o).types;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() { return types; }
}
