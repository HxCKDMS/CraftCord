var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var IntInsnNode = Java.type("org.objectweb.asm.tree.IntInsnNode");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");

var UPDATE_SUGGESTION = ASMAPI.mapMethod("func_195129_h");

function initializeCoreMod() {
    return {
        "craftcord NewChatGui transformer": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.gui.screen.ChatScreen"
            },
            "transformer": function(classnode) {
                classnode.methods.forEach(function(method)  {
                    if (method.name === UPDATE_SUGGESTION) {
                        patchUpdateSuggestion(method.instructions);
                    }
                });

                return classnode;
            }
        }
    };
}

function patchUpdateSuggestion(instructions) {
    var targetNode;

    for (var i = 0; i < instructions.size(); i++) {
        if (instructions.get(i).getOpcode() === Opcodes.PUTFIELD && instructions.get(i-1).getOpcode() === Opcodes.INVOKESTATIC) {
            print("found suggestion insertion location!");
            targetNode =  instructions.get(i);
        }
    }

    var toInsert = new InsnList();

    toInsert.add(new VarInsnNode(Opcodes.ALOAD, 0));
    toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1));
    toInsert.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        "com/karelmikie3/craftcord/patch/ChatScreenPatch",
        "addSuggestions",
        "(Lnet/minecraft/client/gui/screen/ChatScreen;Ljava/lang/String;)V",
        false
    ));


    instructions.insert(targetNode, toInsert);
}