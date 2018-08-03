'use strict';

import {FlowNode} from "./FlowNode.js";
import {FlowAction} from "./FlowAction.js";
import {FlowSockets} from "../FlowSockets.js";

export class FlowParameterInput extends FlowNode {

    constructor(node) {
        super(node);
    }

    getComponent() {
        let scopedDbNode = this.dbNode;
        return new D3NE.Component('ParameterInput', {
            template: FlowParameterInput._nodeTemplate(),
            builder(node) {
                let socket = FlowSockets.getInst();
                let dataSource = new D3NE.Input('DataSource', socket.getSocket('dataSource'));
                let call = new D3NE.Output('Call', socket.getSocket('call'), true);

                let key = new D3NE.Control('<input type="text" value="" class="control-text">', (element, control) =>{

                    if(scopedDbNode !== undefined && scopedDbNode.key !== undefined && scopedDbNode.key !== null) {
                        element.setAttribute("value",scopedDbNode.key);
                        control.putData('key',element.value);
                    }

                    control.putData('dbNode', scopedDbNode);

                    control.id = "key";
                    control.name = "Key";

                    element.addEventListener('change', ()=>{
                        control.putData('key',element.value);
                        node.data['dbNode'].key = element.value;
                    });
                });

                return node
                    .addInput(dataSource)
                    .addOutput(call)
                    .addControl(key);
            },
            worker(node, inputs, outputs) {
                outputs[0] = this;
            }
        });
    }

    static _nodeTemplate() {
        return `
            <div class="title">{{node.title}}</div>
                <content>
                    <column al-if="node.controls.length&gt;0 || node.inputs.length&gt;0">
                        <!-- Inputs-->
                        <div al-repeat="input in node.inputs" style="text-align: left">
                            <div class="socket input {{input.socket.id}} {{input.multipleConnections?'multiple':''}} {{input.connections.length&gt;0?'used':''}}" al-pick-input="al-pick-input" title="{{input.socket.name}}
                {{input.socket.hint}}"></div>
                            <div class="input-title" al-if="!input.showControl()">{{input.title}}</div>
                            <div class="input-control" al-if="input.showControl()" al-control="input.control"></div>
                        </div>
                        <!-- Controls-->
                        <div class="control" al-repeat="control in node.controls" style="text-align: center" :width="control.parent.width - 2 * control.margin" :height="control.height" al-control="control"></div>
                    </column>
                    <column>
                        <!-- Outputs-->
                        <div al-repeat="output in node.outputs" style="text-align: right">
                            <div class="output-title">{{output.title}}</div>
                            <div class="socket output {{output.socket.id}} {{output.connections.length>0?'used':''}}" al-pick-output="al-pick-output" title="{{output.socket.name}}
                {{output.socket.hint}}"></div>
                        </div>
                    </column>
                </content>
            </div>
        `;
    }

}