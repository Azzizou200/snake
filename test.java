package test;



import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.List;
import java.util.ArrayList;


public class test extends JFrame {
    private JTextArea outputArea;
    private File loadedFile;

    private ArrayList<String> tokens;
    private Set<String> declaredVariables;
    private int currentLine;

    public test() {
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
 // Méthode utilitaire pour compter les variables dans une déclaration
    private int countVariables1(List<String> tokens, int startIndex) {
        int count = 0;
        for (int i = startIndex + 1; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.equals("#")) {
                break; // Fin de la déclaration
            } else if (!token.equals(",")) {
                count++;
            }
        }
        return count;
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
        boolean shouldCheckForHash = !line.trim().matches(".*\\b(Snk_Begin|Snk_Begi|Snk_Beg|Snk_Begn|Snk_End|Snk_En|Snk_Ed|If|Else|Else|Els|Eles|Begin|Beg|Begi|Begn|End|En)\\b.*");
        boolean shouldCheckForHas = !line.trim().matches(".*\\b(Snk_Begi|Snk_Beg|Snk_Begn|Snk_En|Snk_Ed|Els|Eles|Beg|Begi|Begn|En)\\b.*");

        if (shouldCheckForHash && !line.trim().endsWith("#") && !line.trim().isEmpty()) {
            outputArea.append("Erreur Lexicale Ligne " + lineNumber + ": Une instruction doit se terminer par '#'\n");
        }	
        else if(!shouldCheckForHas) {
            outputArea.append("Erreur Lexicale Ligne " + lineNumber + ": mot clé mal ecrit \n");

        }

        Pattern pattern = Pattern.compile(
            "(Snk_Begin|Snk_End|Snk_Int|Snk_Real|Snk_Strg|Set|Get|If|Else|Begin|End|Snk_Print)|" + // Keywords
            "(\\d+\\.\\d+|\\d+)|" +  // Numbers
            "((?!Snk)[a-zA-Z_][a-zA-Z0-9_]*)|" +  // Identifiers
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
            case "Get":
                return "mot clé pour affectation de valeur entre 2 variables";
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
        // Réinitialiser la zone de sortie et vérifier si un fichier est chargé
        if (loadedFile == null) {
            outputArea.setText("Veuillez charger un fichier d'abord.\n");
            return;
        }

        outputArea.setText("Analyse syntaxique...\n");

        boolean beginFound = false;
        boolean endFound = false;

        Stack<String> blockStack = new Stack<>();
        Map<String, Integer> blockLineMap = new HashMap<>();

        // Variables pour suivre le nombre de déclarations
        int intVarCount = 0;
        int realVarCount = 0;

        // Variable pour suivre la ligne actuelle
        int currentLine = 1;

        // Parcours des tokens
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            // Incrémenter la ligne si le token est un saut de ligne
            if (token.equals("\n")) {
                currentLine++;
                continue;
            }

            // Détecter chaque ligne et afficher une description
            switch (token) {
                case "Snk_Begin":
                    outputArea.append("Ligne " + currentLine + ": début du programme.\n");
                    beginFound = true;
                    blockStack.push("Snk_Begin");
                    blockLineMap.put("Snk_Begin", currentLine);
                    currentLine ++;
                    break;

                case "Snk_Int":
                    // Compter le nombre de variables entières déclarées
                    int declaredIntVars = countVariables1(tokens, i);
                    intVarCount += declaredIntVars;
                    outputArea.append("Ligne " + currentLine + ": déclaration de " + declaredIntVars + " variable(s) entière(s).\n");
                    currentLine ++;

                    break;

                case "Snk_Real":
                    // Compter le nombre de variables réelles déclarées
                    int declaredRealVars = countVariables1(tokens, i);
                    realVarCount += declaredRealVars;
                    outputArea.append("Ligne " + currentLine + ": déclaration de " + declaredRealVars + " variable(s) réelle(s).\n");
                    currentLine ++;

                    break;

                case "Set":
                    // Vérifiez s'il y a un token suivant pour le nom de la variable
                    if (i + 1 < tokens.size()) {
                        String variableName = tokens.get(i + 1);
                        outputArea.append("Ligne " + currentLine + ": affectation d’une valeur entière à \"" + variableName + "\".\n");
                    } else {
                        outputArea.append("Ligne " + currentLine + ": erreur - aucune variable spécifiée pour l'affectation.\n");
                    }
                    currentLine ++;

                    break;

                case "Get":
                    // Vérifiez qu'il y a au moins deux tokens après "Get" pour récupérer les variables
                    if (i + 2 < tokens.size() && !tokens.get(i + 1).equals(",") && !tokens.get(i + 2).equals(",")) {
                        String targetVariable = tokens.get(i + 1); // La variable qui reçoit la valeur
                        String sourceVariable = tokens.get(i + 3); // La variable source
                        outputArea.append("Ligne " + currentLine + ": Affectation de \"" + targetVariable + "\" depuis \"" + sourceVariable + "\" \n");
                        i += 2; // Passer les tokens utilisés pour éviter de les traiter à nouveau
                    } else {
                        outputArea.append("Ligne " + currentLine + ": erreur - syntaxe incorrecte pour 'Get'.\n");
                    }
                    currentLine ++;

                    break;

                case "If":
                    outputArea.append("Ligne " + currentLine + ": conditionnel (début d'une condition).\n");
                    blockStack.push("If");
                    blockLineMap.put("If", currentLine);
                    currentLine ++;

                    break;

                case "Else":
                    outputArea.append("Ligne " + currentLine + ": sinon (bloc alternatif).\n");
                    if (!blockStack.isEmpty() && blockStack.peek().equals("If")) {
                        blockStack.pop();
                        blockLineMap.remove("If");
                    } else {
                        outputArea.append("Erreur Syntaxique : 'Else' sans 'If' correspondant (ligne " + currentLine + ").\n");
                    }
                    currentLine ++;

                    break;

                case "Begin":
                    outputArea.append("Ligne " + currentLine + ": début d'un bloc.\n");
                    blockStack.push("Begin");
                    blockLineMap.put("Begin", currentLine);
                    currentLine ++;

                    break;

                case "End":
                    outputArea.append("Ligne " + currentLine + ": fin d'un bloc.\n");
                    if (!blockStack.isEmpty() && blockStack.peek().equals("Begin")) {
                        blockStack.pop();
                        blockLineMap.remove("Begin");
                    } else {
                        outputArea.append("Erreur Syntaxique : 'End' sans 'Begin' correspondant (ligne " + currentLine + ").\n");
                    }
                    currentLine ++;

                    break;

                case "Snk_Print":
                    if (i + 1 < tokens.size()) {
                        StringBuilder message = new StringBuilder("Ligne " + currentLine + ": ");

                        List<String> variablesToPrint = new ArrayList<>();
                        List<String> messagesToPrint = new ArrayList<>();
                        boolean hasArguments = false;

                        int j = i + 1; // Index du token suivant
                        while (j < tokens.size() && !tokens.get(j).equals("\n")) {
                            String nextToken = tokens.get(j);

                            if (nextToken.equals("#")) {
                                // Si le token est '#', arrêter l'analyse
                                break;
                            } else if (nextToken.startsWith("\"")) {
                                // Si le token commence par une double-quote, il s'agit d'un message
                                messagesToPrint.add(nextToken);
                                hasArguments = true;
                            } else if (nextToken.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                                // Si c'est un identificateur valide
                                variablesToPrint.add(nextToken);
                                hasArguments = true;
                            } else if (!nextToken.equals(",")) {
                                // Ignorer les virgules mais signaler tout autre token invalide
                                message.append("\n  → Erreur : argument invalide \"").append(nextToken).append("\".");
                            }

                            j++;
                        }

                        // Construire le message de sortie
                        if (!variablesToPrint.isEmpty()) {
                            message.append("\n  → Affichage des valeurs des identificateurs : ")
                                   .append(String.join(", ", variablesToPrint)).append(".");
                        }
                        if (!messagesToPrint.isEmpty()) {
                            message.append("\n  → Affichage des messages : ")
                                   .append(String.join(", ", messagesToPrint)).append(".");
                        }

                        if (hasArguments) {
                            outputArea.append(message.toString() + "\n");
                        } else {
                            outputArea.append("Ligne " + currentLine + ": erreur - aucun argument fourni pour 'Snk_Print'.\n");
                        }

                        i = j - 1; // Avancer l'index pour éviter de retraiter ces tokens
                    } else {
                        // Si aucun token n'est trouvé après "Snk_Print"
                        outputArea.append("Ligne " + currentLine + ": erreur - aucun argument fourni pour 'Snk_Print'.\n");
                    }
                    currentLine ++;

                    break;

                case "Snk_End":
                    outputArea.append("Ligne " + currentLine + ": fin du programme.\n");
                    endFound = true;
                    if (!blockStack.isEmpty() && blockStack.peek().equals("Snk_Begin")) {
                        blockStack.pop();
                        blockLineMap.remove("Snk_Begin");
                    } else {
                        outputArea.append("Erreur Syntaxique : 'Snk_End' trouvé sans 'Snk_Begin' correspondant.\n");
                    }
                    currentLine ++;

                    break;

                case "##":
                    // Traiter les commentaires qui commencent par "##"
                    StringBuilder commentContent = new StringBuilder();
                    int j = i; // L'index courant

                    // Parcourir tous les tokens sur la même ligne
                    while (j < tokens.size() && !tokens.get(j).equals("\n")) {
                        if (tokens.get(j).startsWith("##")) {
                            // Ajouter le contenu du commentaire sans les "##"
                            commentContent.append(tokens.get(j).substring(2)).append(" ");
                        } 
                        j++;
                    }

                    outputArea.append("Ligne " + currentLine + ": commentaire ignoré : " + commentContent.toString().trim() + "\n");
                    i = j - 1; // Avancer l'index pour éviter de retraiter ces tokens
                    currentLine++; // Incrémenter le numéro de la ligne
                    break;


                default:
                    break;
            }
        }

        // Vérifications finales
        if (!beginFound) {
            outputArea.append("Erreur Syntaxique : 'Snk_Begin' manquant.\n");
        }
        if (!endFound) {
            outputArea.append("Erreur Syntaxique : 'Snk_End' manquant.\n");
        }
        while (!blockStack.isEmpty()) {
            String unclosedBlock = blockStack.pop();
            int line = blockLineMap.get(unclosedBlock);
            outputArea.append("Erreur Syntaxique : Bloc non fermé : " + unclosedBlock + " ouvert à la ligne " + line + ".\n");
        }

        // Afficher le résumé des variables déclarées
        outputArea.append("\nRésumé des variables déclarées :\n");
        outputArea.append("- Variables entières : " + intVarCount + "\n");
        outputArea.append("- Variables réelles : " + realVarCount + "\n");
    }


    // mzl mkmlthch 
    private void analyzeSemantic() {
        outputArea.setText("Analyse sémantique...\n");
        int i = 0; // Start index
        Map<String, String> variableTypes = new HashMap<>(); // Keeps track of declared variables and their types
        Map<String, String> variableValue = new HashMap<>(); // Keeps track of declared variables and their types
        int snk_flag=0;
        int bloc_flag=0;
        currentLine = 1;
        while (i < tokens.size()) {
            String token = tokens.get(i);
            if (token.equals("\n")) {
                currentLine++;
                continue;
            }
            switch (token) {
            case "Snk_Int":
            case "Snk_Real": {
                // Determine the type of variables being declared
                String type = token.equals("Snk_Int") ? "int" : "real";

                // Ensure there are tokens for variable declaration
                if (i + 1 < tokens.size()) {
                    
                    
                    
                	int j = i+1;
                    if(tokens.get(i+2).equals("#")) {
                    	if (variableTypes.containsKey(tokens.get(i+1))) {
                    		outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Variable déjà déclarée : " + tokens.get(i+1) + "\n");
                    		
                    	}else {
                    		variableTypes.put(tokens.get(i+1), type);
                    		variableValue.put(tokens.get(i+1), null);
                    		
                    	}
                    }else {
	                    // Iterate through the declared variables
	                    while(tokens.get(j+1).equals(",")) {
	                    	 
	                        if (variableTypes.containsKey(tokens.get(j))) {
	                            outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Variable déjà déclarée : " + tokens.get(j) + "\n");
	                            j = j + 2;
	                        } else {
	                            variableTypes.put(tokens.get(j), type);
	                            variableValue.put(tokens.get(j), null);
	                            j = j + 2;
	                        }
	                        
	                    }if(tokens.get(j+1).equals("#")) {
	                    	if (variableTypes.containsKey(tokens.get(j))) {
	                    		outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Variable déjà déclarée : " + tokens.get(i+1) + "\n");
	                    		
	                    	}else {
	                    		variableTypes.put(tokens.get(j), type);
	                    		variableValue.put(tokens.get(j), null);
	                    		
	                    	}
	                    }
                    }
                    i = j;
                    // Ensure the statement ends with "#"
                    if (i + 1 < tokens.size() && tokens.get(i + 1).equals("#")) {
                    	 System.out.print( variableTypes);
                    	currentLine++;
                        i += 2; // Skip over the declaration and "#"
                    } else {
                        outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Syntaxe incorrecte. Déclaration non terminée par '#'.\n");
                        currentLine++;
                        i++;
                    }
                } else {
                    outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Syntaxe incorrecte dans la déclaration des variables.\n");
                    currentLine++;
                    i++;
                }
                break;
            }	
            case "Snk_Strg": {
                // Determine the type of variables being declared
                
                // Ensure there are tokens for variable declaration
                if (i + 1 < tokens.size()) {
                    
                    
                    
                	int j = i+1;
                    if(tokens.get(i+2).equals("#")) {
                    	if (variableTypes.containsKey(tokens.get(i+1))) {
                    		outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Variable déjà déclarée : " + tokens.get(i+1) + "\n");
                    		
                    	}else {
                    		variableTypes.put(tokens.get(i+1), "Strg");
                    		variableValue.put(tokens.get(i+1), null);
                    		
                    	}
                    }else {
	                    // Iterate through the declared variables
	                    while(tokens.get(j+1).equals(",")) {
	                    	 
	                        if (variableTypes.containsKey(tokens.get(j))) {
	                            outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Variable déjà déclarée : " + tokens.get(j) + "\n");
	                            j = j + 2;
	                        } else {
	                            variableTypes.put(tokens.get(j), "Strg");
	                            variableValue.put(tokens.get(j), null);
	                            j = j + 2;
	                        }
	                        
	                    }
                    }
                    i = j;
                    // Ensure the statement ends with "#"
                    if (i + 1 < tokens.size() && tokens.get(i + 1).equals("#")) {
                    	currentLine++;
                    	 System.out.print( variableTypes);
                    	
                        i += 2; // Skip over the declaration and "#"
                    } else {
                        outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Syntaxe incorrecte. Déclaration non terminée par '#'.\n");
                        currentLine++;
                        i++;
                    }
                } else {
                    outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Syntaxe incorrecte dans la déclaration des variables.\n");
                    currentLine++;
                    i++;
                }
                break;
            }

                case "Set": {
                    // Handle variable assignment
                    if (i + 2 < tokens.size()) {
                        String targetVariable = tokens.get(i + 1);
                        String targetType = variableTypes.get(targetVariable);
                        String value = tokens.get(i + 2);
                        if (!variableTypes.containsKey(targetVariable)) {
                        	outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Variable non déclarée : " + targetVariable + "\n");
                        }else {
                        if(!isNumeric(value)) {
                        	if(targetType=="Strg") {
                        		if(value.startsWith("\"")&&value.endsWith("\"")) {
                        			variableValue.put(targetVariable, value);
                        		}else {
                        			outputArea.append("Ligne " + currentLine + "Erreur Sémantique : la chaîne de caractère n'est pas mise entre deux quottes doubles"+"\n");
                        		}
                        	}else {
                        		outputArea.append("Ligne " + currentLine + "Erreur Sémantique : type incompatable"+"\n");
                        	}
                        }
                        	if(isInteger(value)) {
                        		
                        		variableValue.put(targetVariable, value);
                        		 System.out.print( variableValue);
                        	}
                        
                        	else if(isFloat(value)) {
	                        	if(variableTypes.get(targetVariable)=="real") {
	                        		variableValue.put(targetVariable, value);
	                        	}else {
	                        		outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Valeur non real : " + value + "\n");
                        		}
	                        }
                        
                        }
                        currentLine++;
                        i += 3; // Move past this statement
                    } else {
                        outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Syntaxe incorrecte dans 'Set'.\n");
                        currentLine++;
                        i++;
                    }
                    break;
                }
                case "Get": {
                    // Handle "Get" assignments
                    if (i + 3 < tokens.size()) {
                        String targetVariable = tokens.get(i + 1);
                        String fromKeyword = tokens.get(i + 2);
                        String sourceVariable = tokens.get(i + 3);

                        if (!fromKeyword.equals("from")) {
                            outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Mot-clé attendu 'from' dans 'Get'. Trouvé : " + fromKeyword + "\n");
                        }

                        if (!variableTypes.containsKey(targetVariable)) {
                            outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Variable cible non déclarée : " + targetVariable + "\n");
                        }

                        if (!variableTypes.containsKey(sourceVariable)) {
                            outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Variable source non déclarée : " + sourceVariable + "\n");
                        }
                        if(variableTypes.get(targetVariable)==variableTypes.get(sourceVariable) || variableTypes.get(sourceVariable)=="int") {
                        	variableValue.put(targetVariable,variableValue.get(sourceVariable));
                        }else {
                        	outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Type incompatibale\n");
                        }
                        currentLine++;
                        i += 4; // Move past this statement
                    } else {
                        outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Syntaxe incorrecte dans 'Get'.\n");
                        currentLine++;
                        i++;
                    }
                    break;
                }
                case "Snk_Print": {
                    // Handle print statements
                	
                    if(tokens.get(i+1).startsWith("\"")) {
                    	
                    	if(!tokens.get(i+1).endsWith("\"")) {
                    		
                    		outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Syntaxe incorrecte dans 'Snk_Print'.\n");
                    		
                    	}else {
                    		i=i+2;
                    	}
                    	
                    }else if (variableTypes.containsKey(tokens.get(i+1))) {
                    	int k =i+1;
                    	while(tokens.get(k+1).equals(",")&&k < tokens.size()) {
                    		
                    		if (!variableTypes.containsKey(tokens.get(k+2))) {
                    			outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Variable non déclarée dans 'Snk_Print' : " + tokens.get(k) + "\n");
                    		}
                    		k =k+2;
                    	}
                    	i = k+1;
                    }else {
            			outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Variable non déclarée dans 'Snk_Print' : " + tokens.get(i+1) + "\n");
            			
            		}

                    if (i < tokens.size() && tokens.get(i).equals("#")) {
                    	currentLine++;
                    	i = i + 1; // Move past the print statement
                    } else {
                        outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Syntaxe incorrecte dans 'Snk_Print'.\n");
                        currentLine++;
                        i++;
                    }
                    break;
                }
                case "If": {
                    // Handle If conditions
                    if (tokens.get(i + 1).equals("[")) {
                    	boolean flag = false;
                    	int j = i+1;
                    	while(j+1<tokens.size()) {
                    		j++;
                    		
                    		if(tokens.get(j).equals("]")) {
                    			flag = true;
                    			break;
                    		}else if(tokens.get(j).equals("[")||tokens.get(j).equals("\n")) {
                    			outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Syntaxe incorrecte dans la condition.\n");
                    			i =j+1;
                    			break;
                    		}
                    	}
                    	if(flag == true) {
                    		i = j+1;
                    	}
                        
                    } else {
                        outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Syntaxe incorrecte dans 'If'.\n");
                        currentLine++;
                        i++;
                    }
                    break;
                }
                case "Else": {
                    i++; // "Else" keyword does not require specific handling for now
                    currentLine++;
                    break;
                }
                case "Snk_Begin": {
                	if(snk_flag==0) {
                		snk_flag++;
                	}else {
                		outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Snk_Begin deja declare"+"\n");
                	}
                	currentLine++;
                	i++; // "Begin" keyword does not require specific handling for now
                    break;
                }
                case "Snk_End": {
                	if(snk_flag>0) {
                		snk_flag--;
                	}else {
                		outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Snk_Begin n'est pas declare"+"\n");
                	}
                	currentLine++;
                	i++; // "Begin" keyword does not require specific handling for now
                    break;
                }
                case "Begin": {
                	bloc_flag++;
                	currentLine++;
                	i++; // "Begin" keyword does not require specific handling for now
                    break;
                }
                case "End": {
                	if(bloc_flag>0) {
                		bloc_flag--;
                	}else {
                		outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Begin n'est pas declare"+"\n");
                	}
                	currentLine++;
                    i++; // "End" keyword does not require specific handling for now
                    break;
                }
                default: {
                    if (!token.equals("#")) {
                        outputArea.append("Ligne " + currentLine + "Erreur Sémantique : Mot-clé non reconnu ou syntaxe incorrecte : " + token + "\n");
                    }
                    currentLine++;
                    i++; // Move to the next token
                }
            }
        }
    }
    public static boolean isInteger(String str) {
        return str.matches("-?\\d+");
    }

    public static boolean isFloat(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
	public static boolean isNumeric(String str) {
		  return str.matches("-?\\d+(\\.\\d+)?"); 
		}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            test compiler = new test();
            compiler.setVisible(true);
        });
    }
}
