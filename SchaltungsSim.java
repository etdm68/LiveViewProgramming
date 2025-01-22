import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.IntStream;
import java.awt.List;
import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

enum Types{
    OR,AND,NOR,NAND,XOR,NOT
}
enum HiLo{
    HIGH(1),
    LOW(0);
    final int value;
    private HiLo(int value){
        this.value = value;
    }
}

class Gate implements Serializable{
    private static final long serialVersionUID = 1L;
    String type;
    ArrayList<Integer> inputs = new ArrayList<>();
    int output = 0;
    ArrayList<Point> inputCoordinates = new ArrayList<>();
    Point[] outputCoordinates = new Point[1];
    ArrayList<UpdateInfo> inputUpdateInfo = new ArrayList<>();
    Set<Object> connections = new HashSet<>();
    ArrayList<Object> sources = new ArrayList<>();
    String name = "";
    static int orCount, andCount,xorCount,norCount,nandCount,notCount;
    Point gatePosition;
    Gate(Types type, int row,int col){
        switch (type) {
            case Types.OR:
                this.type = "OR";
                orCount++;
                name = "OR" + orCount;
                break;
            case Types.AND:
                this.type = "AND";
                andCount++;
                name = "AND" + andCount;
                break;
            case Types.NOR:
                this.type = "NOR";
                norCount++;
                name = "NOR" + norCount;
                break;
            case Types.NAND:
                this.type = "NAND";
                nandCount++;
                name = "NAND" + nandCount;
                break;
            case Types.XOR:
                this.type = "XOR";
                xorCount++;
                name = "XOR" + xorCount;
                break;
            case Types.NOT:
                this.type = "NOT";
                notCount++;
                name = "NOT" + notCount;
                break;
            default:
                throw new IllegalArgumentException("Wrong type. Only (OR,AND,NAND,NOR,XOR,NOT) are available");
        }    
        this.gatePosition = new Point(col,row);
        
    }

    void addGateInput(Input input){
        if(this.type.equals("NOT") && inputs.size() >= 1) throw new IllegalArgumentException("Ein NOT-Gatter hat nur einen Input");
        if(inputs.size() >= 2) throw new IllegalArgumentException("Jedes Gatter darf nur zwei inputs besitzen");
        inputs.add(input.value);
    }

    void changeInput(int index){
        assert index == 0 || index == 1;
        if(inputs.size() == 0) throw new IllegalArgumentException("Es muss mindestens ein Input vorhanden sein um diesen zu ändern");
        inputs.set(index, ~inputs.get(index));
    }

    void calculateOutput(){
        
        if(inputs.size() == 2 || this.type.equals("NOT")&& inputs.size() == 1){
            switch (type) {
                case "OR":
                    output = inputs.get(0) | inputs.get(1);
                    break;
                case "AND":
                    output = inputs.get(0) & inputs.get(1);
                    break;
                case "NOT":
                    output = inputs.get(0) == 1? 0 : 1;
                    break;
                case "NAND":
                    output = inputs.stream().allMatch(i -> i == 1) ? 0 : 1;
                    break;
                case "NOR":
                    output = inputs.stream().anyMatch(i -> i == 1) ? 0 : 1;
                    break;
                case "XOR":
                    output = inputs.get(0) ^ inputs.get(1);
                    break;
            } 
        }
        else {
            System.out.println("Inputs for gate: " + name + " are incomplete");
            return;
        }
    }

    void getCoordInOut(double x, double y, Gate gate){
        
        if(gate.type.equals("NOT")){
            Point input1 = new Point((int)x-30,(int)y);
            Point output = new Point((int)x+35,(int)y);
            gate.inputCoordinates.add(input1);
            gate.outputCoordinates[0] = output;
        }else{
            Point input1 = new Point((int)x-30,(int)y-10);
            Point input2 = new Point((int)x-30,(int)y+10);
            Point output = new Point((int)x+35,(int)y);
            gate.inputCoordinates.add(input1);
            gate.inputCoordinates.add(input2);
            gate.outputCoordinates[0] = output;
        }
        
    }
    void updateInputs(){
        inputUpdateInfo.forEach(iUI -> {
            if(iUI.gate != null){
                inputs.set(iUI.index, iUI.gate.output);
            }
            else if(iUI.input != null){
                inputs.set(iUI.index, iUI.input.value);
            }
        });
    }

    @Override
    public String toString(){
        String s = " ";
        return s += name + " postion: " + gatePosition;
    }
}


//Mit der Circuit Klasse wird das Programm betrieben
class Circuit implements Serializable{
    private static final long serialVersionUID = 1L;
    String name;
    ArrayList<Gate> gates;
    ArrayList<Input> inputs;
    private ArrayList<Output> outputs;
    private ArrayList<ArrayList<Boolean>> fieldCheck = new ArrayList<>();
    transient Turtle turtle;
    private int row,col;
    private Map<Integer,Integer> offsets = new HashMap<>();
    private Map<Integer,Integer> yOffsets = new HashMap<>();
    private ArrayList<Wire> connections = new ArrayList<>();
    //Feld erstellen mit einer Standart Größe
    Circuit(String name){
        this(name, 6, 6);
    }
    // Zweiter Konstruktor falls der Benutzer sich direkt ein größers Feld erstellen möchte 
    Circuit(String name, int row, int col){
        assert row >= 3 && col >= 3: "row AND col must be >= 3";
        assert row <= 100 && col <= 100: "row or col cannot be larger than 100";
        this.name = name;
        this.row = row;
        this.col = col;
        this.gates = new ArrayList<>();
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        turtle = new Turtle(col*114,row*114);
        turtle.lineWidth(3);
        for(int i = 1; i <= col; i++){
            offsets.put(i,6);
        }
        for(int i = 1; i <= row; i++){
            yOffsets.put(i,5);
        }
        for(int i = 1; i <= 100; i++){
            ArrayList<Boolean> rows = new ArrayList<>();
            for(int j = 1; j <= 100; j++){
                rows.add(false);
            }
            fieldCheck.add(rows);
        }
        new FieldDraw(this).drawCircuitField();
    }
    //Konstruktor um eine gespeicherte Schaltung zu laden
    public Circuit(String name,String fileName) {
        Circuit tempCircuit = new Circuit("Test");
        Clerk.clear();
        tempCircuit = null;
        Circuit loadedCircuit = loadCircuit(fileName);
        if (loadedCircuit != null) {
            this.gates = loadedCircuit.gates;
            this.connections = loadedCircuit.connections;
            this.offsets = loadedCircuit.offsets;
            this.yOffsets = loadedCircuit.yOffsets;
            this.inputs = loadedCircuit.inputs;
            this.outputs = loadedCircuit.outputs;
            this.col = loadedCircuit.col;
            this.row = loadedCircuit.row;
            this.fieldCheck = loadedCircuit.fieldCheck;
            this.turtle = new Turtle(114 * this.col, 114 * this.row);  
            new FieldDraw(this).drawCircuitField(); 
            drawAllInputs();
            drawAllOutputs();
            drawAllComponents();                
            drawAllConnections();               
        } else {
            System.err.println("Wrong filename: " + fileName);
        }
    }
    private static class FieldDraw{
        private Circuit circuit;
        FieldDraw(Circuit circuit){
            this.circuit = circuit;
        }

        //Malt das Feld 
        void drawCircuitField(){
            circuit.turtle.reset()
                .penDown();
            int x = 50, y = 50;
            circuit.turtle.moveTo(x, y)
                .color(168, 50, 50);
            for(int i = 1; i <= circuit.row; i++){
                for(int j = 1; j <= circuit.col; j++){
                    circuit.turtle.penDown();
                    drawSquare(100);
                    circuit.turtle.penUp()
                        .forward(100);
                
                }
                circuit.turtle.moveTo(x,y+=100);
            }
            drawTop();
            drawLeft();
            drawRight();
            drawBottom();
            drawLettersTopBetter();
            drawLettersLeftBetter();
            circuit.turtle.right(90);
        }

        void drawBottom(){
            int x = 50;
            int y = (circuit.row * 100) + 50;
            circuit.turtle.color(61, 3, 252);
            for(int i = 0; i <= circuit.col; i++){
                circuit.turtle.moveTo(x, y)
                    .penDown()
                    .right(90)
                    .forward(50)
                    .backward(50);
                x += 100;
                circuit.turtle.left(90);
            }
        }

        

        void drawLettersLeftBetter(){
            int x = 18, y = 110;
            circuit.turtle.color(4,1,15)
            .moveTo(x, y);
            IntStream.range(1, circuit.row+1).forEach(i->{circuit.turtle.text(String.valueOf(i), Font.ARIAL, 30, null).moveTo(x, (i*100)+y);});
        }
        void drawLettersTopBetter(){
            int x = 90, y = 30;
            circuit.turtle.color(4,1,15)
                .moveTo(x, y)
                .left(90)
                .text("E", Font.ARIAL,30,null);
            IntStream.range(2, circuit.col+1).forEach(i -> {circuit.turtle.moveTo((90*i)+(i-1)*10, y).text(String.valueOf(i), Font.ARIAL, 30, null);});
        }
        
        void drawRight(){
            int x = (circuit.col * 100) + 50, y = 50;
            circuit.turtle.color(61, 3, 252);
            for(int i = 0; i <=circuit. row; i++){
                circuit.turtle.moveTo(x, y)
                    .penDown()
                    .forward(50)
                    .backward(50);
                y += 100;
            }
        }

        void drawLeft(){
            int x = 50, y = 50;
            circuit.turtle.moveTo(x, y);
            circuit.turtle.color(61, 3, 252);
            for(int i = 0; i <= circuit.row; i++){
                circuit.turtle.penDown()
                    .left(180)
                    .forward(50)
                    .backward(50)
                    .penUp()
                    .moveTo(x, y += 100)
                    .right(180);
            }
        }

        void drawTop(){
            int y = 50, x = 50;
            circuit.turtle.moveTo(x, y)
                .color(61, 3, 252);
            for(int j = 0; j <= circuit.col; j++){
                circuit.turtle.penDown()
                    .left(90)
                    .forward(50)
                    .backward(50)
                    .right(90)
                    .penUp()
                    .forward(100);
            }
            
        }
        void drawSquare(int size){
            circuit.turtle.penDown();
            for(int i = 1; i <= 4; i++){
                circuit.turtle.penDown()
                    .forward(size)
                    .right(90);
            }
        }
        
    }
    void saveCircuit(String fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(this);
            System.out.println("Circuit saved with name: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving circuit: " + e.getMessage());
        }
    }
    public Circuit loadCircuit(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            Circuit loadedCircuit = (Circuit) ois.readObject();
            return loadedCircuit;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading circuit: " + e.getMessage());
        }
        return null;
    }
    //um ein Feld zu erweitern
    void expandField(int row, int col){
        Clerk.clear();
        assert row > 0 || col > 0;
        assert row >= 0 && col >= 0: "its not allowed to choose a number < 0";
        addOffsets(col);
        addYOffsets(row);
        this.row += row;
        this.col += col;
        this.turtle = new Turtle(114*this.col,114*this.row);
        new FieldDraw(this).drawCircuitField();
        outputs.forEach(output -> output.col += col);
        changeOutputCoordinations(col);
        drawAllComponents();
        drawAllInputs();
        drawAllOutputs();
        drawAllConnections();
    }
    
    private void changeOutputCoordinations(int col){
        connections.forEach(connect -> outputs.forEach(output -> {
            if(output.name.equals(connect.endName)) connect.endX += col*100;
        }));
    }
    //Methode zum Einfügen von Inputs
    Circuit addInput(int row,HiLo hilo){
        assert row <= this.row && row>0;
        Input input = new Input(row, hilo);
        if(inputs.stream().anyMatch(inputs -> inputs.row == input.row)) throw new IllegalArgumentException("Their is already a Input on this position");
        inputs.add(input);
        drawInputE(1*100, row*100, input);
        return this;
    }

    void calcAllOutputs(){
        Queue<Gate> con = new LinkedList<>();
        con.addAll(gates);
        while(!con.isEmpty()){
            Gate gate = con.poll();
            int tempOutput = gate.output;
            gate.updateInputs();
            gate.calculateOutput();
            if(tempOutput != gate.output){
                for(Object connection : gate.connections){
                    if(connection instanceof Gate nextGate){
                        con.add(nextGate);
                    }
                    else if(connection instanceof Output output){
                        output.setValue(gate.output);
                    }
                }
            }
        }
        turtle.reset();
        new FieldDraw(this).drawCircuitField();
        drawAllComponents();
        drawAllInputs();
        drawAllOutputs();
        drawAllConnections();

    }

    
    //Methode um die Werte der Inputs zu ändern
    //Man kann erst die setInput Methode verwenden wenn ale Inputs der Gatter belegt sind
    //sonst kommt es zu einem Fehler
    void setInput(int row, HiLo hilo){
        assert row <= this.row && row > 0;
        
        Input in = inputs.stream()
            .filter(input -> input.row == row)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Their is no Input in this row"));
        in.setValue(hilo);
        new FieldDraw(this).drawCircuitField();
        calcAllOutputs();
        drawAllInputs();
        drawAllComponents();
        drawAllOutputs();
        drawAllConnections();
    }
    private int findYOffset(String name){
        Optional<Gate> tempGate = gates.stream()
            .filter(gate -> gate.name.equals(name))
            .findFirst();
        if(tempGate.isPresent()){
            Gate gate = tempGate.get();
            int index = (int)gate.gatePosition.getY();
            int offset = yOffsets.get(index);
            syncYOffsets((int)gate.gatePosition.getY());
            return offset;
        }
        Optional<Input> tempInput = inputs.stream() 
            .filter(input -> input.name.equals(name))
            .findFirst();
        if (tempInput.isPresent()) {
            Input input = tempInput.get();
            int index = input.row;
            
            int offset = yOffsets.get(index);
            syncYOffsets(index);
            return offset;
        }
        
        return 5;
    }

    private void syncYOffsets(int index){
        int offset = yOffsets.get(index);
        yOffsets.put(index, offset + 5);
    }
    private void addYOffsets(int row){
        int tempRow = this.row;
        for(int i = 1; i <= row; i++){
            yOffsets.put(tempRow+i, 5);
        }
    }
    private int findOffset(String name){
        
        Optional<Gate> tempGate = gates.stream().filter(gate -> gate.name.equals(name)).findFirst();
        
        if(tempGate.isPresent()){
            Gate gate1 = tempGate.get();
            int index = (int)gate1.gatePosition.getX();
            int offset = offsets.get(index);
            syncOffsets((int)gate1.gatePosition.getX());
            return offset;
        }
        Optional<Output> tempOutput = outputs.stream().filter(output -> output.name.equals(name)).findFirst();
        if(tempOutput.isPresent()){
            Output output = tempOutput.get();
            int offset = offsets.get(output.col);
            syncOffsets(output.col);
            return offset;
        }
        
        return 5;
    }
    //Updaten der Offsets
    private void syncOffsets(int index){
        int offset = offsets.getOrDefault(index, 5);
        offsets.put(index, offset+5);
    }
    private void addOffsets(int col){
        int tempCol = this.col;
        for(int i = 1; i <= col; i++){
            offsets.put(tempCol+i, 5);
        }
    }
    //Wird benötigt um nachdem ändern eines Inputs das Feld zu verändern
    private void drawAllInputs(){
        inputs.forEach(input -> drawInputE(1*100, input.row*100, input));
    }
    private void drawAllComponents(){
        gates.forEach(gates -> drawGates(gates, gates.gatePosition));
    }
    private void drawAllOutputs(){
        outputs.forEach(output -> drawOutputA(col*100, output.row*100, output));
    }
    private void drawAllConnections(){
        offsets.replaceAll((k,v) -> 5);
        yOffsets.replaceAll((k,v) -> 5);
        connections.forEach(connect -> {
            drawConnections(connect.sourceX, connect.sourceY, connect.endX, connect.endY, connect.startName, connect.endName);
        });
    }
    //Output hinzufügen
    Circuit addOutput(int row){
        assert row <= this.row && row > 0;
        if(outputs.stream().anyMatch(output -> output.row == row))throw new IllegalArgumentException("Their is already an output on this position");
        Output output = new Output(this.col, row);
        outputs.add(output);
        drawOutputA(col*100, row*100, output);
        return this;
    }
  

    //Methode um Gatter hinzuzufügen
    Circuit addComponent(Gate gate){
        if(fieldCheck.get((int)gate.gatePosition.getY()-1).get((int)gate.gatePosition.getX()-1)) throw new IllegalArgumentException("It is not possible to place a Gate on a wire");

        if(gate.gatePosition.getX() == 1 || gate.gatePosition.getX() == col) throw new IllegalArgumentException("Its not possible to place a Gate on this position");

        if(gates.stream().anyMatch(gates -> gates.gatePosition.equals(gate.gatePosition))) throw new IllegalArgumentException("Their is already a Gate on this position");
        else {
            gates.add(gate);
            drawGates(gate, gate.gatePosition);
        }
        return this;
    }
    Circuit fullAdder(){
        Gate gate1 = new Gate(Types.XOR, 1, 2);
        Gate gate2 = new Gate(Types.AND, 3, 2);
        Gate gate3 = new Gate(Types.XOR, 1, 4);
        Gate gate4 = new Gate(Types.AND, 3, 4);
        Gate gate5 = new Gate(Types.OR, 4, 5);
        addComponent(gate1)
            .addComponent(gate2)
            .addComponent(gate3).addComponent(gate4)
            .addComponent(gate5);
        addInput(1, HiLo.LOW)
            .addInput(2, HiLo.LOW)
            .addInput(4, HiLo.LOW);
        addOutput(1).addOutput(3);
        connect(gate1.name, gate3.name).connect(gate1.name,gate4.name)
            .connect(gate2.name,gate5.name)
            .connect(gate4.name,gate5.name)
            .connect(gate3.name,"A1").connect(gate5.name,"A3")
            .connect("E1",gate1.name).connect("E1",gate2.name)
            .connect("E2",gate1.name).connect("E2",gate2.name)
            .connect("E4",gate3.name).connect("E4",gate4.name);
        return this;
    }
    //Input darf nur als start verwendet werden
    //Output darf nur als end verwendet werden
    void connectGates(Gate startGate, Gate endGate){
        if(endGate.gatePosition.getX() <= startGate.gatePosition.getX()) throw new IllegalArgumentException("It is not possible to connect these Gates");
        if(endGate.inputs.size() == 2 || endGate.type.equals("NOT") && 
            endGate.inputs.size() == 1) throw new IllegalArgumentException("The inputs of this gate are already occupied");
                    
            endGate.inputs.add(startGate.output);
            endGate.sources.add(startGate);
            
            drawConnGateToGate(startGate, endGate);
    }
    void connectGateToOut(Gate startGate, Output output){
        if(!output.hasConnection){
            output.setConnection(startGate.output);
            
            drawConnectionToOutput(startGate, output);
        }
        else throw new IllegalArgumentException("The output is already occupied");
    }
    void connectInToGate(Input input, Gate endGate){
        if(endGate.inputs.size() == 2 || endGate.type.equals("NOT") && 
            endGate.inputs.size() == 1) throw new IllegalArgumentException("The inputs of this gate are already occupied");

            endGate.inputs.add(input.value);
            endGate.sources.add(input);
            input.connectionsToGates.add(endGate);
            drawConnInToGate(input, endGate);
    }
    Circuit connect(String start, String end){
        if(start.equals(end)) throw new IllegalArgumentException("It is not allowed to connect a Component with itself");
        Object startComponent = findStartComponent(start);
        Object endComponent = findEndComponent(end);
        //Pattern Matching
        switch (startComponent) {
            case Gate startGate : 
                if(endComponent instanceof Gate endGate){
                    connectGates(startGate, endGate);
                }
                else if(endComponent instanceof Output output){
                    connectGateToOut(startGate, output);
                }
                else throw new IllegalArgumentException("Wrong Component");
                break;
            case Input input :
                if(endComponent instanceof Gate endGate){
                    connectInToGate(input, endGate);
                    break;
                }
                else if(endComponent instanceof Output) throw new IllegalArgumentException("It is not allowed to connect an input with an output");
                else throw new IllegalArgumentException("Wrong Component");
            default:
                throw new IllegalArgumentException("Incorrect values were passed");
                
        }
        return this;   
    }
        
    private Object findEndComponent(String end){
        //Optional<Gate> 
        for(Gate gate : gates){
            if(gate.name.equals(end)){
                return gate;
            }
        }
        for(Output output : outputs){
            if(output.name.equals(end)){
                return output;
            }
        }
        throw new IllegalArgumentException("Their is no component with this name");
    }
    private Object findStartComponent(String start){
        for(Gate gate : gates){
            if(gate.name.equals(start)){
                return gate;
            }
        }
        for(Input input : inputs){
            if(input.name.equals(start)){
                return input;
            }
        }
        throw new IllegalArgumentException("Their is no component with this name");
    }
    
    private void checkField(double x, double y){
        for(int i = 0; i < fieldCheck.size(); i++){
            for(int j = 0; j < fieldCheck.get(i).size(); j++){
                
                if((j + 1) * 100 == x && (i + 1) * 100 == y){
                    fieldCheck.get(i).set(j, true);
                }
            }
        }
    }
    
    private double[] checkIfGateIsNearByX(double x, double y, String start, String end){
        double[] result = new double[2];
        
        for(Gate gate : gates){
            if(((gate.gatePosition.getX()*100)-38) - x == 0 && (gate.gatePosition.getY()*100) - y == 0){
                if(gate.name.equals(start) || gate.name.equals(end)){
                    result[0] = x;
                    result[1] = y;
                    return result;
                }
                else {
                    int offset = findYOffset(start);
                    turtle.right(90)
                        .forward(30 + offset)
                        .left(90)
                        .forward(70);
                    result[0] = x + 70;
                    result[1] = y + 30 + offset;
                    return result;
                }
            }
        }
        result[0] = x;
        result[1] = y;

        return result;
    }
    
    private void drawConnectionToOutput(Gate gate, Output output){
        double startXPos = gate.outputCoordinates[0].getX(), startYPos = gate.outputCoordinates[0].getY();
        double outputXPos = output.col*100, outputYPos = output.row*100;
        gate.connections.add(output);
        connections.add(new Wire(startXPos, startYPos, outputXPos, outputYPos, gate.name, output.name));
        drawConnections(startXPos, startYPos, outputXPos, outputYPos, gate.name, output.name);
    }
    private void colorOfConnection(String name){
        Optional<Gate> tempGate = gates.stream().filter(gate -> gate.name.equals(name)).findFirst();
        Optional<Input> tempInput = inputs.stream().filter(input -> input.name.equals(name)).findFirst();
        if(tempGate.isPresent()){
            Gate gate = tempGate.get();
            if(gate.output == 1){
                turtle.color(11, 128, 39);
            }
            else turtle.color(0,0,0);
        }
        else if(tempInput.isPresent()){
            Input input = tempInput.get();
            if(input.value == 1){
                turtle.color(11, 128, 39);
            }
            else turtle.color(0,0,0);
        }
    }

    private void drawConnections(double xStart, double yStart, double xEnd, double yEnd, String start, String end){
        int offset = findOffset(end);
        colorOfConnection(start);
        turtle.moveTo(xStart, yStart);
        while(xStart != xEnd-offset){
            
            double[] result = checkIfGateIsNearByX(xStart, yStart, start, end);
            xStart = result[0];
            yStart = result[1];
            turtle.penDown().forward(1);
            xStart++;
            checkField(xStart, yStart);
        }
        if(yStart < yEnd){
            turtle.right(90);
            while(yStart != yEnd){
                turtle.penDown().forward(1);
                yStart++;
            }
            turtle.left(90).forward(offset);
        }
        else{
            turtle.left(90);
            while (yStart != yEnd) {
                turtle.penDown().forward(1);
                yStart--;
            }
            turtle.right(90).forward(offset);
        }
        turtle.color(0,0,0);
    }

    private void drawConnGateToGate(Gate startGate, Gate endGate){
        double startXPos = startGate.outputCoordinates[0].getX(), startYPos = startGate.outputCoordinates[0].getY();
        int inputsSize = endGate.inputs.size();
        double endXPos, endYPos;
            endXPos = inputsSize == 1 ? endGate.inputCoordinates.get(0).getX() : endGate.inputCoordinates.get(1).getX(); 
            endYPos = inputsSize == 1 ? endGate.inputCoordinates.get(0).getY() : endGate.inputCoordinates.get(1).getY();
            endGate.inputUpdateInfo.add(new UpdateInfo(startGate, null, inputsSize-1));
            connections.add(new Wire(startXPos, startYPos, endXPos, endYPos, startGate.name, endGate.name)); 
            drawConnections(startXPos, startYPos, endXPos, endYPos, startGate.name, endGate.name);  
    }


    private void drawConnInToGate(Input input, Gate gate){
        double startXPos = input.col * 100, startYPos = input.row * 100;
        int inputsSize = gate.inputs.size();
        double gateXPos, gateYPos;
        gateXPos = inputsSize == 1? gate.inputCoordinates.get(0).getX() : gate.inputCoordinates.get(1).getX();
        gateYPos = inputsSize == 1? gate.inputCoordinates.get(0).getY() : gate.inputCoordinates.get(1).getY();
        gate.inputUpdateInfo.add(new UpdateInfo(null, input, inputsSize-1));
        turtle.moveTo(startXPos, startYPos);
        connections.add(new Wire(startXPos, startYPos, gateXPos, gateYPos, input.name, gate.name));
        drawConnections(startXPos, startYPos, gateXPos, gateYPos, input.name, gate.name);
    }

    private void drawGates(Gate gate,Point point){
        switch (gate.type) {
            case "OR":
                drawOR(point.getX()*100, point.getY()*100, gate);
                break;
            case "AND":
                drawAND(point.getX()*100, point.getY()*100, gate);
                break;
            case "XOR":
                drawXOR(point.getX()*100, point.getY()*100, gate);
                break;
            case "NOR":
                drawNOR(point.getX()*100, point.getY()*100, gate);
                break;
            case "NAND":
                drawNAND(point.getX()*100, point.getY()*100, gate);
                break;
            case "NOT":
                drawNOT(point.getX()*100, point.getY()*100, gate);
                break;
            default:
                System.out.println("Wrong input");
                break;
        }
    }


    private void drawCircle() {
        turtle.penDown();
        double radius = 5; // Radius von 5 Pixel
        double circumference = 2 * Math.PI * radius; // Berechnung des Umfangs
        double totalSteps = 50; // Anzahl der Schritte (mehr Schritte = glatterer Kreis)
        double stepSize = circumference / totalSteps; // Schrittgröße basierend auf dem Umfang
        double anglePerStep = 360.0 / totalSteps; // Winkel pro Schritt
    
        for (int i = 0; i < totalSteps; i++) {
            turtle.forward(stepSize).right(anglePerStep);
        }
    
        // Zurück zur ursprünglichen Ausrichtung
        turtle.left(360);
    }
    

    private void drawSquareSquare(int size){
        turtle.penDown();
        for(int i = 1; i <= 4; i++){
            turtle.penDown();
            turtle.forward(size);
            turtle.right(90);
        }
    }

    private void drawOutput(double x, double y){
        turtle.moveTo(x, y);
        turtle.penUp().forward(25).penDown().forward(10);
    }

    private void drawInput(double x, double y){
        turtle.moveTo(x, y);
        turtle.penUp()
            .left(180)
            .forward(25)
            .right(90)
            .forward(10)
            .left(90)
            .penDown()
            .forward(5)
            .backward(5);
        turtle.penUp()
            .left(90)
            .forward(20)
            .right(90)
            .penDown()
            .forward(5)
            .right(180);
    }

    private void drawGateName(double x, double y, Gate gate){
        turtle.moveTo(x, y)
            .right(90)
            .penUp().forward(10)
            .right(90).forward(18)
            .right(90)
            .text(gate.name, Font.COURIER, 11, null).right(90);
    }

    private void drawOR(double x, double y, Gate gate){
        turtle.moveTo(x, y);
        turtle.left(90).penUp().forward(25).left(90).forward(25).right(180);
        drawSquareSquare(50);
        drawInput(x,y);
        drawOutput(x, y);
        turtle.moveTo(x, y)
            .left(90)
            .penUp()
            .forward(12)
            .left(90)
            .forward(8)
            .right(90)
            .text("≥1", Font.ARIAL, 15, null).right(90);
        
        gate.getCoordInOut(x, y, gate);
        drawGateName(x, y, gate);
    }
    private void drawAND(double x, double y, Gate gate){
        turtle.moveTo( x-25, y-25);
        drawSquareSquare(50);
        drawInput(x, y);
        drawOutput(x, y);
        turtle.moveTo(x, y)
            .left(90)
            .penUp()
            .forward(10)
            .left(90)
            .forward(5)
            .right(90)
            .text("&", Font.ARIAL, 15, null)
            .right(90);
        gate.getCoordInOut(x, y, gate);
        drawGateName(x, y, gate);
    }

    private void drawNOR(double x, double y, Gate gate){
        turtle.moveTo(x-25, y-25);
        drawSquareSquare(50);
        drawInput(x, y);
        turtle.moveTo(x, y)
            .penUp()
            .forward(31)
            .left(90)
            .forward(5)
            .right(90);
        drawCircle();
        turtle.moveTo(x, y)
            .penUp()
            .forward(35)
            .penDown()
            .forward(6)
            .backward(6);
        turtle.moveTo(x,y)
            .penUp()
            .forward(4)
            .left(90)
            .forward(2)
            .text("1", Font.ARIAL, 15, null)
            .left(90)
            .forward(2)
            .penDown()
            .forward(9)
            .backward(9)
            .penUp().right(90)
            .left(90)
            .forward(8)
            .right(90)
            .text(">", Font.ARIAL, 15, null)
            .right(90);
        gate.getCoordInOut(x, y, gate);
        drawGateName(x, y, gate);
    }   
    
    private void drawNAND(double x, double y, Gate gate){
        turtle.moveTo(x-25, y-25);
        drawSquareSquare(50);
        drawInput(x, y);
        turtle.moveTo(x, y)
            .penUp()
            .forward(31)
            .left(90)
            .forward(5)
            .right(90);
        drawCircle();
        turtle.moveTo(x, y)
            .penUp()
            .forward(35)
            .penDown()
            .forward(6)
            .backward(6);
        turtle.moveTo(x, y)
            .left(90)
            .penUp()
            .forward(10)
            .left(90)
            .forward(5)
            .right(90)
            .text("&", Font.ARIAL, 15, null)
            .right(90);
        gate.getCoordInOut(x, y, gate);
        drawGateName(x, y, gate);
    }

    private void drawXOR(double x, double y, Gate gate){
        turtle.moveTo(x-25, y-25);
        drawSquareSquare(50);
        drawInput(x, y);
        drawOutput(x, y);
        turtle.moveTo(x, y)
            .left(90)
            .penUp()
            .forward(10)
            .left(90)
            .forward(9)
            .right(90)
            .text("=1", Font.ARIAL, 15, null)
            .right(90);
        gate.getCoordInOut(x, y, gate);
        drawGateName(x, y, gate);
    }

    private void drawNOT(double x,double y, Gate gate){
        turtle.moveTo(x-25, y-25);
        drawSquareSquare(50);
        turtle.moveTo(x, y)
            .penUp()
            .backward(25)
            .penDown()
            .backward(10);
        turtle.moveTo(x, y)
            .penUp()
            .forward(31)
            .left(90)
            .forward(5)
            .right(90);
        drawCircle();
        turtle.moveTo(x, y)
            .penUp()
            .forward(35)
            .penDown()
            .forward(6)
            .backward(6);
        turtle.moveTo(x, y)
            .left(90)
            .penUp()
            .forward(10)
            .left(90)
            .forward(4)
            .right(90)
            .text("1", Font.ARIAL, 15, null)
            .right(90);
        gate.getCoordInOut(x, y, gate);
        drawGateName(x, y, gate);
    }

    private void drawInputE(double x, double y, Input input){
        turtle.moveTo(x-35, y-20)
            .left(90)
            .text(input.name, Font.ARIAL,15,null)
            .right(90);
        if(input.value == 1){
            turtle.color(11, 128, 39);
        }
        else turtle.color(10, 10, 10);
        turtle.moveTo(x, y)
            .left(180)
            .penDown()
            .forward(20)
            .penUp().forward(15)
            .right(90)
            .text(String.valueOf(input.value), Font.ARIAL, 15, null)
            .forward(20)
            .right(90)
            .color(10, 10, 10);
    }

    private void drawOutputA(double x, double y, Output output){
        if(output.value == 1){
            turtle.color(11,128,39);
        }
        else turtle.color(10,10,10);
        turtle.moveTo(x, y)
            .penDown()
            .forward(20)
            .penUp().forward(10)
            .left(90)
            .text(String.valueOf(output.value), Font.ARIAL, 15, null)
            .forward(15)
            .left(90)
            .forward(15)
            .right(90)
            .text(output.name, Font.ARIAL, 15, null)
            .color(10,10,10)
            .right(90);
    }
}

interface Inputs {
    void setValue(HiLo value);
    int getValue();
}

class Input implements Inputs,Serializable{
    private static final long serialVersionUID = 1L;
    int col, row, value;
    String name;
    ArrayList<Gate> connectionsToGates = new ArrayList<>();
    Input(int row, HiLo value){
        this.row = row;
        this.col = 1;
        this.value = value.value;
        this.name = "E" + row;
    }

    public void setValue(HiLo value){
        this.value = value.value;
    }

    public int getValue(){
        return value;
    }

    @Override
    public String toString(){
        String s = "";
        return s += name + "col= " + col + " row= " + row + " value= " + value;
    }

}
interface Outputs {
    boolean setConnection(int value);
    void setValue(int value);
}
class Output implements Outputs,Serializable{
    private static final long serialVersionUID = 1L;
    boolean hasConnection = false;
    String name;
    int col, row, value;
    Output(int col, int row){
        this.col = col;
        this.row = row;
        this.value = 0;
        this.name = "A" + row;
    }

    public boolean setConnection(int value){
        if(!hasConnection){
            this.value = value;
            return hasConnection = true;
        }
        return hasConnection = false;
    }

    public void setValue(int value){
        this.value = value;
    }
    
}
//Wire klasse wird nur benötigt um die Verbindungen zwischen den Gattern zu speichern.
//Dies hilft mir später dabei die Verbindungen zu zeichnen wenn ich alle Verbindungen ernuet zeichnen muss
class Wire implements Serializable{
    private static final long serialVersionUID = 1L;
    double sourceX, sourceY, endX, endY;
    String startName = "", endName = "";
    int index;
    Wire(double sourceX, double sourceY, double endX, double endY, String startName, String endName){
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.endX = endX;
        this.endY = endY;
        this.startName = startName;
        this.endName = endName;
    }
    
}

class UpdateInfo implements Serializable{
    private static final long serialVersionUID = 1L;
    Gate gate;
    Input input;
    int index;
    UpdateInfo(Gate gate, Input input, int index){
        this.gate = gate;
        this.input = input;
        this.index = index;
    }
}


