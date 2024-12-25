import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class SnakeCompiler extends JFrame {
    private JTextArea outputArea;
    private File loadedFile;

    private ArrayList<String> tokens;
    private Set<String> declaredVariables;
    private int currentLine;

    public SnakeCompiler() {
        tokens = new ArrayList<>();
        declaredVariables = new HashSet<>();
        currentLine = 1;

        setTitle("SNAKE Compiler");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton loadButton = new JButton("Charger Fichier");
        JButton lexicalButton = new JButton("Analyse Lexicale");
        JButton syntaxButton = new JButton("Analyse Syntaxique");
        JButton semanticButton = new JButton("Analyse Sémantique");

        buttonPanel.add(loadButton);
        buttonPanel.add(lexicalButton);
        buttonPanel.add(syntaxButton);
        buttonPanel.add(semanticButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadButton.addActionListener(e -> loadFile());
        lexicalButton.addActionListener(e -> analyzeLexical());
        syntaxButton.addActionListener(e -> analyzeSyntax());
        semanticButton.addActionListener(e -> analyzeSemantic());
    }

    private void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            loadedFile = fileChooser.getSelectedFile();
            outputArea.setText("Fichier chargé: " + loadedFile.getName() + "\n");
        }
    }

    // Lexical analysis method (no change here)
    private void analyzeLexical() {
        if (loadedFile == null) {
            outputArea.setText("Veuillez charger un fichier d'abord.\n");
            return;
        }
        tokens.clear();
        outputArea.setText("Analyse lexicale...\n");
        try (BufferedReader reader = new BufferedReader(new FileReader(loadedFile))) {
            String line;
            currentLine = 1;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("##")) {
                    outputArea.append("Ligne " + currentLine + ": Commentaire ignoré.\n");
                } else {
                    tokenize(line, currentLine);
                }
                currentLine++;
            }
        } catch (IOException e) {
            outputArea.setText("Erreur lors de la lecture du fichier.\n");
        }
    }

    // Tokenize method (no change here)
    private void tokenize(String line, int lineNumber) {
        // Check if the line needs to end with '#' (excluding specific keywords)
        boolean shouldCheckForHash = !line.trim().matches(".*\\b(Snk_Begin|Snk_End|If|Else|Begin|End)\\b.*");

        if (shouldCheckForHash && !line.trim().endsWith("#") && !line.trim().isEmpty()) {
            outputArea.append("Erreur Lexicale Ligne " + lineNumber + ": Une instruction doit se terminer par '#'\n");
        }

        Pattern pattern = Pattern.compile(
            "(Snk_Begin|Snk_End|Snk_Int|Snk_Real|Snk_Strg|Set|If|Else|Begin|End|Snk_Print)|" + // Keywords
            "(\\d+\\.\\d+|\\d+)|" +  // Numbers
            "([a-zA-Z_][a-zA-Z0-9_]*)|" +  // Identifiers
            "(\"[^\"]*\")|" +  // String literals
            "(\\[|\\]|,|#)|" +  // Special characters
            "(<|>|==|!=|<=|>=|=)"  // Comparison operators
        );

        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            String token = matcher.group();
            String tokenDescription = "Inconnu";

            if (matcher.group(1) != null) {
                tokenDescription = getKeywordDescription(token);
            } else if (matcher.group(2) != null) {
                tokenDescription = "Nombre entier";
            } else if (matcher.group(3) != null) {
                tokenDescription = "Identificateur";
            } else if (matcher.group(4) != null) {
                tokenDescription = "Chaine de caractères";
            } else if (matcher.group(5) != null) {
                if (token.equals(",")) {
                    tokenDescription = "séparateur";
                } else if (token.equals("#")) {
                    tokenDescription = "fin d’instruction";
                } else {
                    tokenDescription = "Caractère spécial";
                }
            } else if (matcher.group(6) != null) {
                tokenDescription = "Opérateur de comparaison";
            }

            tokens.add(token);
            outputArea.append("Ligne " + lineNumber + " : " + token + ": " + tokenDescription + "\n");
        }
    }

    private String getKeywordDescription(String keyword) {
        switch (keyword) {
            case "Snk_Begin":
                return "mot clé de début de programme";
            case "Snk_End":
                return "mot clé de fin de programme";
            case "Snk_Int":
                return "mot clé de déclaration du type entier";
            case "Snk_Real":
                return "mot clé de déclaration du type réel";
            case "Snk_Strg":
                return "mot clé de déclaration du type chaîne";
            case "Set":
                return "mot clé pour affectation d’une valeur";
            case "If":
                return "mot clé pour conditionnel";
            case "Else":
                return "mot clé pour conditionnel (Else)";
            case "Begin":
                return "mot clé pour début de bloc";
            case "End":
                return "mot clé pour fin de bloc";
            case "Snk_Print":
                return "mot clé pour affichage";
            default:
                return "Inconnu";
        }
    }

    // mzl mkmlthch
    private void analyzeSyntax() {
        outputArea.setText("Analyse syntaxique...\n");

        boolean beginFound = false;
        boolean endFound = false;
        Stack<String> blockStack = new Stack<>();
        
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (token.equals("Snk_Begin")) {
                if (beginFound) {
                    outputArea.append("Erreur Syntaxique: Snk_Begin trouvé plusieurs fois.\n");
                } else {
                    outputArea.append("Snk_Begin: début du programme\n");
                    beginFound = true;
                    blockStack.push("Snk_Begin");
                }
            } else if (token.equals("Snk_End")) {
                if (blockStack.isEmpty() || !blockStack.peek().equals("Snk_Begin")) {
                    outputArea.append("Erreur Syntaxique: Snk_End sans Snk_Begin correspondant.\n");
                } else {
                    outputArea.append("Snk_End: fin du programme\n");
                    blockStack.pop();
                    endFound = true;
                }
            } else if (token.equals("If")) {
                blockStack.push("If");
            } else if (token.equals("Else")) {
                if (blockStack.isEmpty() || !blockStack.peek().equals("If")) {
                    outputArea.append("Erreur Syntaxique: Else sans If correspondant.\n");
                } else {
                    blockStack.pop(); // Pop the matching If
                }
            } else if (token.equals("Begin")) {
                blockStack.push("Begin");
            } else if (token.equals("End")) {
                if (blockStack.isEmpty() || !blockStack.peek().equals("Begin")) {
                    outputArea.append("Erreur Syntaxique: End sans Begin correspondant.\n");
                } else {
                    blockStack.pop(); // Pop the matching Begin
                }
            }
        }

        // Check for unmatched If/Else/Begin/End
        while (!blockStack.isEmpty()) {
            String unclosedBlock = blockStack.pop();
            if (unclosedBlock.equals("Snk_Begin")) {
                outputArea.append("Erreur Syntaxique: Snk_Begin manquant un Snk_End correspondant.\n");
            } else if (unclosedBlock.equals("If")) {
                outputArea.append("Erreur Syntaxique: If sans Else correspondant.\n");
            } else if (unclosedBlock.equals("Begin")) {
                outputArea.append("Erreur Syntaxique: Begin manquant un End correspondant.\n");
            }
        }

        if (!beginFound) {
            outputArea.append("Erreur Syntaxique: Snk_Begin manquant.\n");
        }

        if (!endFound) {
            outputArea.append("Erreur Syntaxique: Snk_End manquant.\n");
        }
    }

    // mzl mkmlthch 
    private void analyzeSemantic() {
        outputArea.setText("Analyse sémantique...\n");
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.equals("Snk_Int") || token.equals("Snk_Real")) {
                if (i + 1 < tokens.size()) {
                    String variable = tokens.get(i + 1);
                    if (variable.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                        declaredVariables.add(variable);
                        outputArea.append("Variable déclarée: " + variable + "\n");
                    }
                }
            } else if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                if (!declaredVariables.contains(token) && !token.equals("Snk_Print")) {
                    outputArea.append("Erreur Sémantique : Variable non déclarée utilisée : " + token + "\n");
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SnakeCompiler compiler = new SnakeCompiler();
            compiler.setVisible(true);
        });
    }
}
