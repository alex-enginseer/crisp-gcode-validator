package crisp;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

class ToolPathConsumer extends ConsumerModule<List<GCodeCommand>> {
    Map<Integer, List<Vector3D>> toolPath = new HashMap<Integer, List<Vector3D>>();

    static int PRECISION = 100;

    ToolPathConsumer(LazyParser p) {
        super(p);
        data = new ArrayList<GCodeCommand>();
    }

    void examineToken(Token t) {
        if (t.type instanceof PrinterGCodeToken) {
            // we don't care about any other types
            if (check(PrinterGCodeToken.M_CMD) || check(PrinterGCodeToken.G_CMD)) {
                GCodeCommand cmd = new GCodeCommand(t);
                advance();
                while (isParam()) {
                    cmd.addParam(peek());
                    advance();
                }
                data.add(cmd);
                // System.out.println(cmd);
            }
        }
    }

    void parseTokens() {
        super.parseTokens();
        generateToolPath();
    }

    public void generateToolPath() {
        Coord3D start = new Coord3D(0.0, 0.0, 0.0);
        Coord3D current = start;
        List<Vector3D> layer = new ArrayList<Vector3D>();
        for (GCodeCommand cmd : data) {
            Double x = current.x;
            Double y = current.y;
            Double z = current.z;
            if (cmd.type == PrinterGCodeToken.G_CMD && cmd.idx == 1) {
                // Linear motion
                for (Token param : cmd.params) {
                    if (param.type == PrinterGCodeToken.X_PM) {
                        x = param.value;
                    } else if (param.type == PrinterGCodeToken.Y_PM) {
                        y = param.value;
                    } else if (param.type == PrinterGCodeToken.Z_PM) {
                        z = param.value;
                    }
                }
                // Only the params that are changed will be provided (in absolute coords)

                start = current;
                current = new Coord3D(x, y, z); // coords are absolute
                // THIS IS A GCODE CONFIG SETTING WE SHOULD RECOGNIZE

                Double zp = z * PRECISION;
                Integer zKey = zp.intValue();
                Double zsp = start.z * PRECISION;
                Integer zStartKey = zsp.intValue();
                if (zKey != zStartKey) { // float math!
                    if (toolPath.containsKey(zKey)) {
                        // System.out.println("We already visited layer " + z);
                        layer = toolPath.get(zKey);
                    } else {
                        // System.out.println("Time to add layer" + z);
                        layer = new ArrayList<Vector3D>();
                        toolPath.put(zKey, layer);
                    }
                }
                layer.add(new Vector3D(start, current));
            }
        }
        System.out.println("There are " + toolPath.size() + " layers in the toolpath.");
        double l = 0.0;
        for (List<Vector3D> lr : toolPath.values()) {
            double j = 0.0;
            for (Vector3D v : lr) {
                j += v.length();
            }
            System.out.println("In this layer, the extruder travels " + (j / (10 * 100)) + " m");
            l += j;
        }
        System.out.println("The extruder travels a total of " + (l / (10 * 100 * 1000)) +
            " km in this print.");
    }

    boolean isParam() {
        return check(PrinterGCodeToken.X_PM) || check(PrinterGCodeToken.Y_PM) ||
            check(PrinterGCodeToken.Z_PM) || check(PrinterGCodeToken.R_PM) ||
            check(PrinterGCodeToken.I_PM) || check(PrinterGCodeToken.Z_PM) ||
            check(PrinterGCodeToken.PARAM);
    }
}
