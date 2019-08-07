/*
 * Copyright 2019-2019 karelmikie3
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");

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
            break;
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