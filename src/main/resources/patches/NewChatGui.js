var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var IntInsnNode = Java.type("org.objectweb.asm.tree.IntInsnNode");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");

var RENDER = ASMAPI.mapMethod("func_146230_a");
var ENABLE_BLEND = ASMAPI.mapMethod("enableBlend");

var SET_CHAT_LINE = ASMAPI.mapMethod("func_146237_a");
var SPLIT_TEXT = ASMAPI.mapMethod("func_178908_a");

function initializeCoreMod() {
    return {
        "craftcord NewChatGui transformer": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.gui.NewChatGui"
            },
            "transformer": function(classnode) {
                classnode.methods.forEach(function(method)  {
                    if (method.name === RENDER) {
                        patchRender(method.instructions);
                    } else if (method.name === SET_CHAT_LINE) {
                        patchSetChatLine(method.instructions);
                        patchSetChatLine2(method.instructions);
                    }
                });

                return classnode;
            }
        }
    };
}

function patchRender(instructions) {
    var targetNode;

    for (var i = 0; i < instructions.size(); i++) {
        if (instructions.get(i).getOpcode() === Opcodes.INVOKESTATIC && instructions.get(i).name === ENABLE_BLEND) {

            targetNode = instructions.get(i);
            print("found emote location!");
            break;
        }
    }

    var toInsert = new InsnList();

    toInsert.add(new VarInsnNode(Opcodes.ALOAD, 22));
    toInsert.add(new VarInsnNode(Opcodes.ILOAD, 21));
    toInsert.add(new IntInsnNode(Opcodes.BIPUSH, 8));
    toInsert.add(new InsnNode(Opcodes.ISUB));
    toInsert.add(new InsnNode(Opcodes.I2F));
    toInsert.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        "com/karelmikie3/craftcord/patch/NewChatGuiPatch",
        "addEmotes",
        "(Ljava/lang/String;F)Ljava/lang/String;",
        false
    ));
    toInsert.add(new VarInsnNode(Opcodes.ASTORE, 22));


    instructions.insert(targetNode, toInsert);
}

function patchSetChatLine2(instructions) {
    var targetNode;

    for (var i = 0; i < instructions.size(); i++) {
        if (instructions.get(i).getOpcode() === Opcodes.ALOAD && instructions.get(i + 7).getOpcode() === Opcodes.INVOKESTATIC && instructions.get(i + 7).name === SPLIT_TEXT ) {

            targetNode = instructions.get(i);
            print("found split text 2 location!");
            break;
        }
    }

    var toInsert = new InsnList();

    toInsert.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        "com/karelmikie3/craftcord/patch/NewChatGuiPatch",
        "removeEmotes",
        "(Lnet/minecraft/util/text/ITextComponent;)Lnet/minecraft/util/text/ITextComponent;",
        false
    ));

    instructions.insert(targetNode, toInsert);
}

function patchSetChatLine(instructions) {
    var targetNode;

    for (var i = 0; i < instructions.size(); i++) {
        if (instructions.get(i).getOpcode() === Opcodes.INVOKESTATIC && instructions.get(i).name === SPLIT_TEXT) {

            targetNode = instructions.get(i);
            print("found split text location!");
            break;
        }
    }

    var toInsert = new InsnList();

    toInsert.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        "com/karelmikie3/craftcord/patch/NewChatGuiPatch",
        "changeList",
        "(Ljava/util/List;)Ljava/util/List;",
        false
    ));

    instructions.insert(targetNode, toInsert);
}