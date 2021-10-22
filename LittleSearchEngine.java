package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
	throws FileNotFoundException {
		
		HashMap<String,Occurrence> f = new HashMap<String,Occurrence>();
		Scanner a = new Scanner(new File(docFile));
		//String current = "";
		if(docFile == null){
			throw new FileNotFoundException("Empty file");
		}
		
			while(a.hasNext()){
				String current = getKeyword(a.next());
				if(current != null){
					if(f.containsKey(current)){
						f.get(current).frequency++;
					}
					else{
						f.put(current,new Occurrence(docFile,1));
					}
				}
			}
		
		//System.out.println(f.toString());
		return f;
	}

	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
		
		for(String key:kws.keySet()) {
			if(keywordsIndex.get(key) == null) {
				keywordsIndex.put(key, new ArrayList<Occurrence>());
				keywordsIndex.get(key).add(new Occurrence(kws.get(key).document, kws.get(key).frequency));
			}
			else{
				keywordsIndex.get(key).add(new Occurrence(kws.get(key).document, kws.get(key).frequency));
			}
			ArrayList<Integer> holdVal = insertLastOccurrence(keywordsIndex.get(key));
		}

	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation(s), consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * NO OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
	 * 
	 * If a word has multiple trailing punctuation characters, they must all be stripped
	 * So "word!!" will become "word", and "word?!?!" will also become "word"
	 * 
	 * See assignment description for examples
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {
		String newS = "";
		word = word.toLowerCase();
		for(int x = word.length()-1; x>0;x--){
			if(word.charAt(x)==('.')||word.charAt(x)==(',')|| word.charAt(x)==('?')||word.charAt(x)==(':')||word.charAt(x)==(';')||word.charAt(x)==('!')){
				word = word.substring(0,x);
			}
			else{
				break;
			}
		}
		for(int a = 0; a<word.length();a++){
			if(!Character.isLetter(word.charAt(a))){
				return null;
			}
		}

		
		if(noiseWords.contains(word)){
			return null;
		}
		//System.out.println(word);
		return word;
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		ArrayList<Integer> newIndexes = new ArrayList<Integer>();
		if(occs.size() == 1){
			return null;
		}
		Occurrence a = occs.get(occs.size()-1);
		int val = a.frequency;

		int low = 0;
		int high = occs.size()-2;
		int mid = -1;
		while(high>=low){
			mid = (low+high)/2;
			newIndexes.add(mid);
			if(high==mid){
				break;
			}
			if(occs.get(mid).frequency == val){
				break;
			}
			else if(val<occs.get(mid).frequency){
				low = mid+1;
			}
			else{
				high = mid-1;
			}
		}
		int check = occs.get(mid).frequency;
		if(val>=check){
			occs.add(mid,occs.remove(occs.size()-1));
		}
		else if(val<check){
			occs.add(mid+1,occs.remove(occs.size()-1));
		}
		//System.out.println(newIndexes.toString());
		//System.out.println(occs.toString());
		return newIndexes;
	
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}

		/*ArrayList<Occurrence> list = new ArrayList<Occurrence>();
		//list.add(new Occurrence("random",));
		list.add(new Occurrence("random",5));
		list.add(new Occurrence("random",4));
		list.add(new Occurrence("random",3));
		list.add(new Occurrence("random",2));
		list.add(new Occurrence("random",2));
		list.add(new Occurrence("random",5));
		insertLastOccurrence(list);
		*/

		sc.close();
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. 
	 * 
	 * Note that a matching document will only appear once in the result. 
	 * 
	 * Ties in frequency values are broken in favor of the first keyword. 
	 * That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same 
	 * frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * See assignment description for examples
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, 
	 *         returns null or empty array list.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
	
		ArrayList<String> finalDocs = new ArrayList<>();
        kw1 = kw1.toLowerCase();
        kw2 = kw2.toLowerCase();
        ArrayList<Occurrence> o1 = keywordsIndex.get(kw1);
        ArrayList<Occurrence> o2 = keywordsIndex.get(kw2);
		//System.out.println(o1.toString());
		//System.out.println(o2.toString());
        if((kw1 == null && kw2 == null) || ((!(keywordsIndex.containsKey(kw1))) && (!(keywordsIndex.containsKey(kw2))))){
        	//System.out.println("Fail");
			return null;
		}

        else if(keywordsIndex.containsKey(kw1) && !keywordsIndex.containsKey(kw2)){
            for(Occurrence o : o1){
                if(finalDocs.size() < 5)
                    finalDocs.add(o.document);
            }
			return finalDocs;
        }
		else if(!keywordsIndex.containsKey(kw1) && keywordsIndex.containsKey(kw2)){
            for(Occurrence o : o2){
                if(finalDocs.size()<5)
                    finalDocs.add(o.document);
            }
			return finalDocs;
        }
        else{
			int count1 = 0;
			int count2 = 0;
			while(o1.size() != count1 && o2.size() != count2 && finalDocs.size() != 5) {
					Occurrence new1 = o1.get(count1);
					Occurrence new2 = o2.get(count2);
 
				if(new1.frequency > new2.frequency){
					if(!finalDocs.contains(new1.document)){
						finalDocs.add(new1.document);
					}
					count1++;
				}
				else if(new1.frequency < new2.frequency){
					 if(!finalDocs.contains(new2.document)){
						 finalDocs.add(new2.document);
					 }
					 count2++;
				}
				else if(new1.frequency == new2.frequency){
					 if(!finalDocs.contains(new1.document)){
						 finalDocs.add(new1.document);
					 }
					 count1++;
					 if(!finalDocs.contains(new2.document)){
						 finalDocs.add(new2.document);
					 }
					 count2++;
				}
			}
		 }
		 return finalDocs;
	 }
}
