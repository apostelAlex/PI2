import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

//Gruppennummer: 107

// Huffman tree node
class HuffmanNode {
	int frequency;
	char data;
	HuffmanNode left, right;
}

public class HuffmanCode {
	

	/** list of all chars in the file as Character */
	private char[] chars;

	/** reads a file byte by byte and returns the file as a char[] */
	private static char[] readFile(String filename) {
		ArrayList<Character> chars = new ArrayList<Character>();
		try (FileInputStream fis = new FileInputStream(filename)) {
			int input;
			while ((input = fis.read()) != -1) {
				chars.add((char) input);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Die angegebene Datei konnte nicht gefunden werden.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		char[] result = new char[chars.size()];
		for (int i = 0; i < result.length; i++) 
			result[i] = chars.get(i);
		return result;
	}

	public static char[] differentChars(char[] arr){
		ArrayList<Character> chars = new ArrayList<Character>();
		boolean found = false;
		for (char c : arr) {
			found = false;
			for(char ch : chars){
				if (ch == c){
					found = true;
					break;
				}
			}
			if (found == false){
				chars.add(c);
			}
		}
		char[] result = new char[chars.size()];
		for (int i = 0; i < result.length; i++) 
			result[i] = chars.get(i);
		return result;
	}

	public static HashMap<Character, Integer> charMap(char[] chars){
		HashMap<Character, Integer> res = new HashMap<>();

		for (char c : chars) {
            res.put(c, res.getOrDefault(c, 0) + 1);
        }
		return res;
	}

	public static double shannonEntropy(HashMap<Character, Integer> chars, int charLength){
		double entropy = 0;
		for (int count : chars.values()) {
			double propability = (double) count/charLength;
			entropy -= propability * (Math.log(propability)/Math.log(2));
		}

		return entropy;
	}
	
	public static Map<Character, Integer> freqMap(char[] chars){
		Map<Character, Integer> frequencyMap = new HashMap<>();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
			}
			return frequencyMap;
	}
	
	private static void traverseHuffmanTree(HuffmanNode node, String code, Map<Character, String> codeMap) {
        if (node == null) {
            return;
        }
        if (node.left == null && node.right == null) {
            codeMap.put(node.data, code);
        }
        traverseHuffmanTree(node.left, code + "0", codeMap);
        traverseHuffmanTree(node.right, code + "1", codeMap);
    }

	public static void main(String[] args) {
		HuffmanCode huffman = new HuffmanCode();
		try {
			huffman.chars = readFile(args[0]);
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			System.out.println("Gueltiger Aufruf: java HuffmanCode datei");
		}
			int charLength = huffman.chars.length;
			System.out.println("Anzahl Zeichen\t\t\t  : " + charLength);
			char[] uniqueChars = differentChars(huffman.chars);
			System.out.println("Anzahl verschiedener Zeichen\t  : " + uniqueChars.length);
			int bitsPerChar = (int) Math.ceil(Math.log(uniqueChars.length)/Math.log(2));
			int fixedLength = bitsPerChar*huffman.chars.length;
			System.out.println("Kodierung mit fester Bitlaenge\t  : " + fixedLength + " Bits ("
					+ bitsPerChar + " Bits pro Zeichen)");

			/*  create Huffman tree for optional excercise */
			// priority queue to store the Huffman nodes (frequency of the node as priority)
			PriorityQueue<HuffmanNode> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.frequency));
			// frequency of each character in a hashmap
			HashMap<Character, Integer> hashMapChars = charMap(huffman.chars);
			
			// fill the priority queue
			for (HashMap.Entry<Character, Integer> entry : hashMapChars.entrySet()) {
				HuffmanNode node = new HuffmanNode();
				node.data = entry.getKey();
				node.frequency = entry.getValue();
				queue.add(node);
			}

			HuffmanNode root = null;
			// Creates a Huffman tree by repeatedly removing two nodes with the smallest frequency 
			// from the priority queue, merging them into a new node, and adding the new node back 
			// to the priority queue
			while (queue.size() > 1) {
				HuffmanNode left = queue.poll();
				HuffmanNode right = queue.poll();
				HuffmanNode parent = new HuffmanNode();
				parent.frequency = left.frequency + right.frequency;
				parent.data = '-';
				parent.left = left;
				parent.right = right;
				root = parent;
				queue.add(parent);
			}

			// Traverse the Huffman tree and assign a code to each character based on its position in the tree.
			// A character on the left side of the tree will be assigned a 0, and a character on the right side
			// of the tree will be assigned a 1.

			Map<Character, String> codeMap = new HashMap<>();
        	traverseHuffmanTree(root, "", codeMap);

			// encodes the character array as a string 
			StringBuilder encodedString = new StringBuilder();
			for (int i = 0; i < huffman.chars.length; i++) {
				char c = huffman.chars[i];
				encodedString.append(codeMap.get(c));
			}
			String enc = encodedString.toString();
			long huffmanLength = enc.length();
			double entropy = shannonEntropy(hashMapChars, charLength);
			double temp = (double) huffmanLength/charLength;
			temp = Math.round(temp * 100.0) / 100.0;
			System.out.println("Kodierung mit Huffman-Code\t  : " + huffmanLength + " ("
					+ temp + " Bits pro Zeichen)");

			long initialEncoding = charLength*8;
			double fixedSaving = 1-((double) fixedLength/initialEncoding);
			fixedSaving = Math.round(fixedSaving * 10000.0) / 100.0;
			System.out.println("Ersparnis (optimale feste Laenge) : "
					+ fixedSaving+ "%");
			double huffmanSaving = 1-((double) huffmanLength/initialEncoding);
			huffmanSaving = Math.round(huffmanSaving * 10000.0) / 100.0;
			System.out.println("Ersparnis (Huffman-Code)\t  : "
					+ huffmanSaving + "%");
			entropy = Math.round(entropy * 100.0) / 100.0;
			System.out.println("Entropie\t\t\t  : " + entropy);
			System.out.println("Haeufigste Zeichen:");
			
			int[] freqUniqueChars = new int[uniqueChars.length];
			int i = 0;
			for (char c : uniqueChars) {
				freqUniqueChars[i] = hashMapChars.get(c);
				i++;
			}

			int[] initialFreqUniqueChars = freqUniqueChars.clone();
			char[] initialUniqueChars = uniqueChars.clone();
			
			for (i = 0; i < 10; i++) {
				// find highest value in freqUniqueChars
				int max = 0;
				int index = -1;
				for (int j = 0; j < freqUniqueChars.length; j++) {
					if(freqUniqueChars[j]>max){
						index = j;
						max = freqUniqueChars[j];
					}
				}
				String hex = Integer.toHexString(uniqueChars[index]);
				int valChar = (int)uniqueChars[index];

				if (valChar>32){
					double haeufigkeit = (double)max / charLength;
					haeufigkeit = Math.round(haeufigkeit * 1000.0) / 10.0;
					System.out.println("0x"+hex+" "+uniqueChars[index]+", Häufigkeit: "+max+" ("+haeufigkeit+"%), Codewortlänge: "+codeMap.get(uniqueChars[index]).length());

				}
				else{
					double haeufigkeit = (double)max / charLength;
					haeufigkeit = Math.round(haeufigkeit * 1000.0) / 10.0;
					System.out.println("0x"+hex+"  , Häufigkeit: "+max+" ("+haeufigkeit+"%), Codewortlänge: "+codeMap.get(uniqueChars[index]).length());
				}

				// create new arrays without max value
				int[] newFreqUniqueChars = new int[freqUniqueChars.length - 1];
				char[] newUniqueChars = new char[uniqueChars.length -1];
				for (int j = 0; j < index; j++) {
					newFreqUniqueChars[j] = freqUniqueChars[j];
					newUniqueChars[j] = uniqueChars[j];
				}
				for (int j = index + 1; j < uniqueChars.length; j++) {
					newFreqUniqueChars[j-1] = freqUniqueChars[j];
					newUniqueChars[j-1] = uniqueChars[j];
				}
				// assign new arrays to old arrays
				freqUniqueChars = newFreqUniqueChars;
				uniqueChars = newUniqueChars;
			}
		
			
			System.out.println();
			System.out.println();
			System.out.println("Huffman code:");
			System.out.println(enc);
		
	}
}
