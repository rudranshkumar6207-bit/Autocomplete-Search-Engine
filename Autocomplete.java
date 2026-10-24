import java.util.*;

class TrieNode {
    HashMap<Character, TrieNode> children = new HashMap<>();
    boolean isWord = false;
}

public class Autocomplete{
    

    static TrieNode root = new TrieNode();
    static HashMap<String, Integer> count = new HashMap<>();

    // add a word into the trie
    static void addWord(String word, int times) {
        TrieNode curr = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (!curr.children.containsKey(c)) {
                curr.children.put(c, new TrieNode());
            }
            curr = curr.children.get(c);
        }
        curr.isWord = true;
        count.put(word, count.getOrDefault(word, 0) + times);
    }

    // go through the trie and collect all words below a node
    static void findWords(TrieNode node, String current, ArrayList<String> list) {
        if (node.isWord) {
            list.add(current);
        }
        for (char c : node.children.keySet()) {
            findWords(node.children.get(c), current + c, list);
        }
    }

    // get top suggestions for a prefix
    static ArrayList<String> getSuggestions(String prefix, int limit) {
        ArrayList<String> list = new ArrayList<>();
        TrieNode curr = root;

        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            if (!curr.children.containsKey(c)) {
                return list;   // prefix not found
            }
            curr = curr.children.get(c);
        }

        findWords(curr, prefix, list);

        // sort by frequency, highest first
        Collections.sort(list, new Comparator<String>() {
            public int compare(String a, String b) {
                return count.get(b) - count.get(a);
            }
        });

        if (list.size() > limit) {
            return new ArrayList<>(list.subList(0, limit));
        }
        return list;
    }

    // edit distance between two words (used for spelling mistakes)
    static int editDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                               Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    // find the closest word when nothing matches
    static String closestWord(String word) {
        String best = "";
        int bestDist = Integer.MAX_VALUE;

        for (String w : count.keySet()) {
            int d = editDistance(word, w);
            if (d < bestDist) {
                bestDist = d;
                best = w;
            }
        }
        return best;
    }

    public static void main(String[] args) {

        addWord("apple", 50);
        addWord("app", 30);
        addWord("application", 20);
        addWord("apply", 15);
        addWord("banana", 40);
        addWord("band", 25);
        addWord("cat", 35);
        addWord("dog", 45);

        Scanner sc = new Scanner(System.in);
        System.out.println("Type a word to search (type exit to stop)");

        while (true) {
            System.out.print("search> ");
            String input = sc.nextLine().trim().toLowerCase();

            if (input.equals("exit")) {
                break;
            }

            ArrayList<String> result = getSuggestions(input, 5);

            if (result.isEmpty()) {
                System.out.println("No results. Did you mean: " + closestWord(input) + " ?");
            } else {
                System.out.println("Suggestions: " + result);
            }
        }
        
    }
}
