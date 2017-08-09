package com.worksap.nlp.sudachi.dictionary;

public enum CategoryType {
    DEFAULT(1),
    SPACE(1 << 1),
    KANJI(1 << 2),
    SYMBOL(1 << 3),
    NUMERIC(1 << 4),
    ALPHA(1 << 5),
    HIRAGANA(1 << 6),
    KATAKANA(1 << 7),
    KANJINUMERIC(1 << 8),
    GREEK(1 << 9),
    CYRILLIC(1 << 10),
    USER1(1 << 11),
    USER2(1 << 12),
    USER3(1 << 13),
    USER4(1 << 14);

    private final int id;

    private CategoryType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CategoryType getType(int id)  {
        for (CategoryType type : CategoryType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }
}
