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

package com.worksap.nlp.lucene.sudachi.ja;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.worksap.nlp.lucene.sudachi.ja.SudachiAnalyzer;

import junit.framework.TestCase;

public class TestSudachiAnalyzer extends TestCase {
    private static final String RESOURCE_NAME_SUDACHI_SETTINGS = "sudachiSettings.json";
    private static final String RESOURCE_NAME_SYSTEM_DIC = "system.dic";
    private static final String RESOURCE_NAME_CHAR_DEF = "char.def";
    private static final String RESOURCE_NAME_UNK_DEF = "unk.def";
    private static final String RESOURCE_NAME_REWRITE_DEF = "rewrite.def";
    
    private SudachiAnalyzer analyzer;
    private Directory dir;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Rule
    public TemporaryFolder tempFolderForDictionary = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        tempFolderForDictionary.create();
        File tempFileForDictionary = tempFolderForDictionary.newFolder("sudachiDictionary");

        Files.copy(TestAnalysisSudachi.class
                .getResourceAsStream(RESOURCE_NAME_SUDACHI_SETTINGS), Paths
                .get(tempFileForDictionary.getPath())
                .resolve(RESOURCE_NAME_SUDACHI_SETTINGS));
        Files.copy(TestAnalysisSudachi.class
                .getResourceAsStream(RESOURCE_NAME_SYSTEM_DIC),
                Paths.get(tempFileForDictionary.getPath()).resolve(RESOURCE_NAME_SYSTEM_DIC));
        Files.copy(TestAnalysisSudachi.class
                .getResourceAsStream(RESOURCE_NAME_CHAR_DEF),
                Paths.get(tempFileForDictionary.getPath()).resolve(RESOURCE_NAME_CHAR_DEF));
        Files.copy(TestAnalysisSudachi.class
                .getResourceAsStream(RESOURCE_NAME_UNK_DEF),
                Paths.get(tempFileForDictionary.getPath()).resolve(RESOURCE_NAME_UNK_DEF));
        Files.copy(TestAnalysisSudachi.class
                .getResourceAsStream(RESOURCE_NAME_REWRITE_DEF),
                Paths.get(tempFileForDictionary.getPath())
                        .resolve(RESOURCE_NAME_REWRITE_DEF));
        
        analyzer = new SudachiAnalyzer(SudachiTokenizer.Mode.EXTENDED, tempFileForDictionary.getPath(),
                SudachiAnalyzer.DefaultSudachiSettingsReader(),
                SudachiAnalyzer.getDefaultStopSet(),
                SudachiAnalyzer.getDefaultStopTags());
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        tempFolder.create();
        File tempFile = tempFolder.newFolder("sudachi");
        dir = FSDirectory.open(Paths.get(tempFile.getPath()));

        IndexWriter writer = new IndexWriter(dir, config);
        createIndex(writer);
        checkIndex(writer);
        writer.close();
    }

    private void createIndex(IndexWriter writer) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("txt", "東京都へ行った。私は宇宙人です。", Field.Store.YES));
        writer.addDocument(doc);
        writer.commit();
        writer.isOpen();
    }

    private void checkIndex(IndexWriter writer) throws IOException {
        try (IndexReader reader = DirectoryReader.open(writer, false, false);) {
            List<LeafReaderContext> atomicReaderContextList = reader.leaves();

            for (LeafReaderContext leafReaderContext : atomicReaderContextList) {
                LeafReader leafReader = leafReaderContext.reader();
                Fields fields = leafReader.fields();
                Iterator<String> itrFieldName = fields.iterator();

                while (itrFieldName.hasNext()) {
                    String fieldName = itrFieldName.next();
                    System.out.println("----- fieldName:" + fieldName
                            + " -----");
                    Terms terms = fields.terms(fieldName);
                    TermsEnum termsEnum = terms.iterator();
                    BytesRef bytesRef = null;

                    while ((bytesRef = termsEnum.next()) != null) {
                        String fieldText = bytesRef.utf8ToString();
                        System.out.println(fieldText);
                    }
                }
            }
        }
    }

    @Test
    public void test() throws Exception {
        String searchText = "岩波";
        try (IndexReader reader = DirectoryReader.open(dir);) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser queryParser = new QueryParser("txt", analyzer);
            Query query = queryParser.parse(searchText);
            TopDocs topDocs = searcher.search(query, 5);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;

            for (ScoreDoc scoreDoc : scoreDocs) {
                int docId = scoreDoc.doc;
                System.out.println("----- docId:" + docId + " -----");
                Document doc = searcher.doc(docId);
                List<IndexableField> indexableFieldList = doc.getFields();

                for (IndexableField indexableField : indexableFieldList) {
                    String fieldName = indexableField.name();
                    System.out.println("----- fieldName:" + fieldName
                            + " -----");
                    String fieldText = indexableField.stringValue();
                    System.out.println("fieldText:" + fieldText);
                }
            }
        } finally {
            if (analyzer != null) {
                analyzer.close();
            }
            if (dir != null) {
                dir.close();
            }
        }
    }
}
