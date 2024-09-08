import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DIYFractal extends JFrame {
    final int PANEL_WIDTH = 700, HEIGHT = 750;
    DrawingPanel panel = new DrawingPanel(0, PANEL_WIDTH, HEIGHT);
    JTextField[] fields;
    JTextField[] ruleFields;
    JButton render;
    JPanel east;
    JPanel west;
    JLabel numReplacementRules;
    JButton[] buttonsAfterNumRepRules;
    final int MAX_RULES = 28;
    JButton plantMode;
    JButton discoMode;
    HashMap<String, String> map;

    public DIYFractal() {
        setTitle("Make Your Own Fractal!");
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(panel);
        initWestGUI();
        initEastGUI();
        pack();
        //centers the frame
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((int) (.5 * (screensize.width - getWidth())), (int) (.5 * (screensize.height - getHeight())), getWidth(), getHeight());
        setLayout(new BorderLayout());
    }

    public boolean validateInput(int correctLength, String[] input) {
        if (input.length != correctLength) {
            return false;
        }
        for (String s : input) {
            if (s.isEmpty()) return false;
        }

        return true;
    }

    public void initEastGUI() {
        east = new JPanel(new FlowLayout());
        east.setPreferredSize(new Dimension(300, HEIGHT));

        JLabel label = new JLabel("Replacement rules:");
        render = new JButton("Render");
        render.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                processInput();
            }
        });

        east.add(label);
        this.add(east, BorderLayout.EAST);
    }

    public void setReplacementRuleFields(int nRules) {
        if (ruleFields != null) { // if rule fields already there, then clear it
            east.remove(render);
            for (int i = 0; i < ruleFields.length; i++) {
                east.remove(ruleFields[i]);
            }
        }

        ruleFields = new JTextField[nRules];
        for (int i = 0; i < ruleFields.length; i++) {
            ruleFields[i] = new JTextField(24);
        }
        for (int i = 0; i < ruleFields.length; i++) {
            east.add(ruleFields[i]);
        }

        east.add(render);
        repaint();
    }

    public void clearEastPanel() {
        east.remove(render);
        if(ruleFields != null) {
            for(int i = 0; i < ruleFields.length; i++) {
                east.remove(ruleFields[i]);
            }
        }
        ruleFields = null;

        repaint();
    }

    public void initWestGUI() {

        west = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        west.setPreferredSize(new Dimension(200, HEIGHT));

        JButton lSystem = new JButton("What's an L system?");
        lSystem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "An L-system consists of:\n- an alphabet of symbols that can be used to make strings\n" +
                        "- a collection of production rules that expand each symbol into some larger string of symbols\n- an initial \"axiom\" string from which to begin construction\n" +
                        "- a mechanism for translating the generated strings into geometric structures (this application!)", "What's an L system?", JOptionPane.QUESTION_MESSAGE);
                JOptionPane.showMessageDialog(null, "You can use L systems to make a variety of fractals! Visit https://paulbourke.net/fractals/lsys/ for some ideas!",
                        "What's an L system?", JOptionPane.QUESTION_MESSAGE);
            }
        });

        JButton about = new JButton("About/Tips");
        about.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "Included special symbols in this system:\n" + "F : Move forward by step pixels drawing a line\n" +
                        "f : Move forward by step pixels without drawing a line\n" + "+ : Turn left by angle\n" + "- : Turn right by angle\n" + "| : Reverse direction (ie: turn by 180 degrees)\n" +
                        "[ : Push current drawing state onto stack\n" + "] : Pop current drawing state from the stack\n\n" + String.format("You can enter up to %d rules.\n\n", MAX_RULES) +
                        "(Tip: Sometimes when copying and pasting, the string parser doesn't recognize spaces so just add spaces manually through the keyboard.)");
            }
        });

        JButton format = new JButton("How to Format Input");
        format.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "'_' INDICATES ONE SPACE.\n" + "Format coordinate point as 'X_Y'.\n" +
                        "Format replacement rules as 'F_F+F-F-FF+F+F-F' with left being what's replaced and right being the replacement.");
            }
        });

        JLabel coorLabel = new JLabel("Coordinate point to start fractal:");
        JTextField coordinates = new JTextField(16);

        JLabel axiomLabel = new JLabel("Axiom:");
        JTextField axiom = new JTextField(16);

        numReplacementRules = new JLabel("Number of Replacement Rules: ");
        JTextField numRules = new JTextField(16);

        JLabel angleLabel = new JLabel("Angle: ");
        JTextField angle = new JTextField(10);

        JLabel stepLabel = new JLabel("Step:");
        JTextField step = new JTextField(6);

        fields = new JTextField[]{coordinates, axiom, angle, step, numRules};

        JButton enterRepRules = new JButton("Enter replacement rules!");
        enterRepRules.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(enterRepRules.getText().equals("Enter replacement rules!")) {
                    if(validateNRulesInput() != -1) {
                        setReplacementRuleFields(validateNRulesInput());
                        hideNumReplacementRules();
                        enterRepRules.setText("Reenter number of rules!");
                        repaint();
                    }
                }
                else {
                    clearEastPanel();
                    showNumReplacementRules();
                    enterRepRules.setText("Enter replacement rules!");
                    repaint();
                }
            }
        });

        JButton increase = new JButton("+");
        increase.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                panel.level++;
                repaint();
            }
        });


        JButton decrease = new JButton("-");
        decrease.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (panel.level > 0) {
                    panel.level--;
                    repaint();
                }
            }
        });

        plantMode = new JButton("Turn Plant Mode On");
        plantMode.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(plantMode.getText().equals("Turn Plant Mode On")) {
                    plantMode.setText("Turn Plant Mode Off");
                }
                else {
                    plantMode.setText("Turn Plant Mode On");
                }
            }
        });

        discoMode = new JButton("DISCO MODE!");
        discoMode.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(discoMode.getText().equals("DISCO MODE!")) {
                    discoMode.setText("Turn Off Disco Mode");
                    repaint();
                }
                else {
                    discoMode.setText("DISCO MODE!");
                    repaint();
                }
            }
        });

        JButton clear = new JButton("Clear");
        clear.setBackground(Color.RED);
        clear.setForeground(Color.WHITE);

        clear.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear the panel and fields?",
                        "Are you sure?", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    panel.clear();

                    for (int i = 0; i < fields.length; i++) {
                        fields[i].setText("");
                    }

                    if(enterRepRules.getText().equals("Reenter number of rules!")) {
                        showNumReplacementRules();
                    }

                    map = null;
                    enterRepRules.setText("Enter replacement rules!");
                    plantMode.setText("Turn Plant Mode On");
                    discoMode.setText("DISCO MODE!");

                    clearEastPanel();
                    repaint();
                }
            }
        });

        buttonsAfterNumRepRules = new JButton[]{enterRepRules, increase, decrease, plantMode, discoMode, clear};

        west.add(lSystem);
        west.add(about);
        west.add(format);
        west.add(coorLabel);
        west.add(coordinates);
        west.add(axiomLabel);
        west.add(axiom);
        west.add(angleLabel);
        west.add(angle);
        west.add(stepLabel);
        west.add(step);
        west.add(numReplacementRules);
        west.add(numRules);
        west.add(enterRepRules);
        west.add(increase);
        west.add(decrease);
        west.add(plantMode);
        west.add(discoMode);
        west.add(clear);
        this.add(west, BorderLayout.WEST);
    }

    public void hideNumReplacementRules() {
        west.remove(numReplacementRules);
        west.remove(fields[4]);
    }

    public void showNumReplacementRules() {
        for(int i = 0; i < buttonsAfterNumRepRules.length; i++) {
            west.remove(buttonsAfterNumRepRules[i]);
        }
        west.add(numReplacementRules);
        west.add(fields[4]);

        for(int i = 0; i < buttonsAfterNumRepRules.length; i++) {
            west.add(buttonsAfterNumRepRules[i]);
        }
    }

    public int validateNRulesInput() {
        int nRules;
        try {
            nRules = Integer.parseInt(fields[4].getText().strip());
        } catch (Exception NumberFormatException) {
            JOptionPane.showMessageDialog(null, "Please enter one number for the number of rules.", "Incorrect Input", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        if(nRules < 1) {
            JOptionPane.showMessageDialog(null, "Please enter a number greater than 0.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        if(nRules > MAX_RULES) {
            JOptionPane.showMessageDialog(null, String.format("This application can only accommodate a maximum of %d rules.\nSorry for the inconvenience!", MAX_RULES));
            return -1;
        }

        return nRules;
    }

    /**
     * FUNCTION CALLED AFTER HITTING 'Render' ON EAST SIDE PANEL
     * validates JTextField inputs -> if the input is invalid, it displays a message dialog warning and returns
     * if map is successfully initialized, it sets the fields of the drawing panel and calls repaint to draw fractal
     */
    public void processInput() {
        String[] coor = fields[0].getText().strip().split(" ");
        if (!validateInput(2, coor)) {
            JOptionPane.showMessageDialog(null, "Wrong input format.\n" + "Refer to 'How to Format Input' for formatting.",
                    "Incorrect Formatting", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int[] coordinate = new int[2]; //////////////
        try {
            coordinate[0] = Integer.parseInt(coor[0]);
            coordinate[1] = Integer.parseInt(coor[1]);
        } catch (Exception NumberFormatException) {
            JOptionPane.showMessageDialog(null, "Please enter numbers for the coordinate.", "Incorrect Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ax = fields[1].getText().strip(); ///////////////

        int a = 0; ////////
        try {
            a = Integer.parseInt(fields[2].getText().strip());
        } catch (Exception NumberFormatException) {
            JOptionPane.showMessageDialog(null, "Please enter one integer for the rotating angle.", "Incorrect Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int step = 0; /////////
        try {
            step = Integer.parseInt(fields[3].getText().strip());
        } catch (Exception NumberFormatException) {
            JOptionPane.showMessageDialog(null, "Please enter one integer for the step amount.", "Incorrect Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HashMap<String, String> tempMap = getReplacementRuleMap();
        if(tempMap != null) {
            map = tempMap;
            panel.setFields(ax, a, coordinate[0], coordinate[1], step);
            panel.level = 0;
            repaint();
        }
    }

    /**
     *
     * @return HasHMap<String, String> if all JTextFields on east panel were valid, else returns null
     */
    public HashMap<String, String> getReplacementRuleMap() {
        HashMap<String, String> ret = new HashMap<>();
        for (int i = 0; i < ruleFields.length; i++) {
            String[] input = ruleFields[i].getText().strip().split(" ");

            if (!validateInput(2, input)) {
                JOptionPane.showMessageDialog(null, "Incorrect Formatting for Replacement Rules.\nSee 'How to Format Input' for details.", "Incorrect Formatting", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            ret.put(input[0], input[1]);
        }
        return ret;
    }

    class DrawingPanel extends JPanel {
        int WIDTH, HEIGHT;
        int level;
        String axiom;
        int angle;
        int x, y;
        int step;

        public DrawingPanel(int level, int width, int height) {
            this.level = level;
            WIDTH = width;
            HEIGHT = height;
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            this.setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            removeAll();

            if(discoMode.getText().equals("Turn Off Disco Mode")) {
                this.setBackground(Color.BLACK);
                g.setColor(Color.WHITE);
            }
            else {
                this.setBackground(Color.WHITE);
                g.setColor(Color.BLACK);
            }

            g.drawString(String.format("Level: %d", level), 330, 720);
            if (map != null) {
                create(g);
            }
        }

        public void clear() {
            removeAll();

            Graphics g = this.getGraphics();
            g.setColor(Color.BLACK);
            level = 0;
            g.drawString(String.format("Level: %d", level), 330, 720);
        }

        public String expandLSystem(String axiom, Map<String, String> map) {
            StringBuilder axiomBuilder = new StringBuilder().append(axiom);
            for (int i = 0; i <= level; i++) {
                StringBuilder expansion = new StringBuilder();
                for (char c : axiomBuilder.toString().toCharArray()) {
                    if (map.containsKey(String.valueOf(c))) {
                        expansion.append(map.get(String.valueOf(c)));
                    } else {
                        expansion.append(c);
                    }
                }
                axiomBuilder = expansion;
            }

            return axiomBuilder.toString();
        }

        public void create(Graphics g) {
            g.setColor(Color.BLACK);

            String pattern = expandLSystem(axiom, map);

            Turtle t;
            if (plantMode.getText().equals("Turn Plant Mode Off")) {
                t = new Turtle(x, y, 90, Color.BLACK);
            }
            else {
                t = new Turtle(x, y, angle, Color.BLACK);
            }

            int[] prevPair = new int[]{x, y};

            Stack<int[]> stack = new Stack<>(); // will hold int[3]s of the x-coordinate, y-coordinate, and angle in a certain position if needed

            for (char c : pattern.toCharArray()) {
                if (c == 'F' || c == 'f') {
                    t.moveForward(step);
                } else if (c == '+') {
                    t.rotate(angle);
                } else if (c == '-') {
                    t.rotate(-angle);
                }
                else if (c == '[') {
                    stack.push(new int[]{t.getX(), t.getY(), t.getAngle()});
                }
                else if (c == ']') {
                    int[] position = stack.pop();
                    t = new Turtle(position[0], position[1], position[2], Color.BLACK);
                }
                else if (c == '|') {
                    t.rotate(180);
                }
                int[] curPair = new int[]{t.getX(), t.getY()};
                if(c != ']' && c != 'f') {
                    if(discoMode.getText().equals("Turn Off Disco Mode")) {
                        g.setColor(new Color((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256)));
                    }
                    g.drawLine(prevPair[0], prevPair[1], curPair[0], curPair[1]);
                }
                prevPair = curPair;
            }
        }

        public void setFields(String axiom, int angle, int x, int y, int step) {
            this.axiom = axiom;
            this.angle = angle;
            this.x = x;
            this.y = y;
            this.step = step;
        }
    }

    public static void main(String[] args) {
        DIYFractal fractal = new DIYFractal();
    }
}