/*
 *  Copyright (c) 2017 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.lucene.sudachi.ja.tokenattribute;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

import com.worksap.nlp.sudachi.Morpheme;

public class PartOfSpeechAttributeImpl extends AttributeImpl implements
        PartOfSpeechAttribute, Cloneable {
    private Morpheme morpheme;

    public List<String> getPartOfSpeechForArray() {
        if (morpheme == null) {
            return null;
        }

        int i = 0;
        List<String> posList = new ArrayList<>();
        StringBuilder posBuilder = new StringBuilder();

        for (String pos : morpheme.partOfSpeech()) {
            if (i == 4) {
                posList.add(posBuilder.toString());
            }
            if (pos.equals("*")) {
                i++;
                continue;
            }
            if (i < 4) {
                if (posBuilder.length() != 0) {
                    posBuilder.append(",");
                }
                posBuilder.append(pos);

            } else {
                posList.add(pos);
            }
            i++;
        }
        return posList;
    }

    public String getPartOfSpeech() {
        if (morpheme == null) {
            return null;
        }

        StringBuilder posBuilder = new StringBuilder();
        for (String pos : morpheme.partOfSpeech()) {
            if (posBuilder.length() != 0) {
                posBuilder.append(",");
            }
            posBuilder.append(pos);
        }
        return posBuilder.toString();
    }

    public void setMorpheme(Morpheme morpheme) {
        this.morpheme = morpheme;
    }

    @Override
    public void clear() {
        morpheme = null;
    }

    @Override
    public void copyTo(AttributeImpl target) {
        PartOfSpeechAttribute t = (PartOfSpeechAttribute) target;
        t.setMorpheme(morpheme);
    }

    @Override
    public void reflectWith(AttributeReflector reflector) {
        String partOfSpeech = getPartOfSpeech();
        reflector.reflect(PartOfSpeechAttribute.class, "partOfSpeech",
                partOfSpeech);
    }
}
