package uiwidgets {
import blocks.*;

import flash.display.*;
import flash.events.*;
import flash.geom.*;
import flash.net.*;
import flash.text.*;
import flash.utils.ByteArray;

import ui.parts.UIPart;
import ui.parts.CheckBox;

import util.*;

public class BlockColorSensorCorrector extends Sprite {

        private var base:Shape;
        private var blockShape:BlockShape;
        private var blockLabel:TextField;

        // Bright controls
        private var rBoxBright:EditableLabel;
        private var gBoxBright:EditableLabel;
        private var bBoxBright:EditableLabel;


        // Color controls
        private var rBoxColor:EditableLabel;
        private var gBoxColor:EditableLabel;
        private var bBoxColor:EditableLabel;


        private var rSliderBright:Scrollbar;
        private var gSliderBright:Scrollbar;
        private var bSliderBright:Scrollbar;

        private var rSliderColor:Scrollbar;
        private var gSliderColor:Scrollbar;
        private var bSliderColor:Scrollbar;


        // current bright correction
        private var r:Number;
        private var g:Number;
        private var b:Number;


        // current color correction
        private var rColor:Number;
        private var gColor:Number;
        private var bColor:Number;


        private var cbLockR:CheckBox = new CheckBox();
        private var cbLockG:CheckBox = new CheckBox();
        private var cbLockB:CheckBox = new CheckBox();


        private var arrColorListFields:Array = [];


        private var application:Scratch;

        public function BlockColorSensorCorrector(application:Scratch){
                this.application = application;
                addChild(base = new Shape());
                setWidthHeight(1000, 400);




                addBrightCorrection();
                addColorCorrection();


                makeLabel('R',      15, 585, 20, true);
                makeLabel('G',      15, 685, 20, true);
                makeLabel('B',      15, 785, 20, true);
                makeLabel('Bright', 15, 873, 20, true);


                for(var f:int = 0; f < this.application.arrColorNames.length; f++){
                   var tf:TextField = new TextField();
                   tf.x = 500;
                   tf.y = 50 + 40 * f;
                   tf.text = this.application.arrColorNames[f] + ":";
                   tf.defaultTextFormat = new TextFormat(CSS.font, 14, CSS.textColor, true);
                   tf.autoSize = TextFieldAutoSize.LEFT;
                   addChild(tf);


                   arrColorListFields[this.application.arrColorNames[f]] = [];


                   for(var i:int = 0; i < 4; i++){
                      tf = new TextField();
                      tf.x = 570 + i * 100;
                      tf.y = 50 + 40 * f;
                      tf.selectable = true;
                      tf.type = TextFieldType.INPUT;
                      tf.text = this.application.arrColorRegion[this.application.arrColorNames[f]][i*2] + "-" + this.application.arrColorRegion[this.application.arrColorNames[f]][i*2+1]
                      tf.defaultTextFormat = new TextFormat(CSS.font, 14, CSS.textColor, true);
                      tf.border = true;
                      tf.width = 60;
                      tf.height = 20;
                      arrColorListFields[this.application.arrColorNames[f]][i] = tf;
                      tf.addEventListener(Event.CHANGE, function(e:Event):void{
                         update()
                      });
                      addChild(tf);
                   }
                }

                update();
        }

        private function setWidthHeight(w:int, h:int):void {
                var g:Graphics = base.graphics;
                g.clear();
                g.beginFill(CSS.white);
                g.drawRect(0, 0, w, h);
                g.endFill();
        }

        public function apply(b:IconButton):void {
        }


        private function addBrightCorrection():void {
                makeLabel('R', 15, 35,  0, true);
                makeLabel('G', 15, 110, 0, true);
                makeLabel('B', 15, 185, 0, true);

                addChild(rBoxBright = new EditableLabel(rTextChangedBright));
                addChild(gBoxBright = new EditableLabel(gTextChangedBright));
                addChild(bBoxBright = new EditableLabel(bTextChangedBright));

                addChild(rSliderBright = new Scrollbar(10, 300, setr));
                addChild(gSliderBright = new Scrollbar(10, 300, setg));
                addChild(bSliderBright = new Scrollbar(10, 300, setb));

                rBoxBright.setWidth(50);
                gBoxBright.setWidth(50);
                bBoxBright.setWidth(50);

                rBoxBright.x = 25;
                gBoxBright.x = 100;
                bBoxBright.x = 175;
                rBoxBright.y = gBoxBright.y = bBoxBright.y = 25;

                rSliderBright.x = rBoxBright.x + 20;
                gSliderBright.x = gBoxBright.x + 20;
                bSliderBright.x = bBoxBright.x + 20;
                rSliderBright.y = gSliderBright.y = bSliderBright.y = rBoxBright.y + 30;

                r = application.colorCorrectionBrightR;
                g = application.colorCorrectionBrightG;
                b = application.colorCorrectionBrightB;
        }
        private function addColorCorrection():void {
                makeLabel('R', 15, 300, 0, true);
                makeLabel('G', 15, 375, 0, true);
                makeLabel('B', 15, 450, 0, true);

                addChild(rBoxColor = new EditableLabel(rTextChangedColor));
                addChild(gBoxColor = new EditableLabel(gTextChangedColor));
                addChild(bBoxColor = new EditableLabel(bTextChangedColor));

                addChild(rSliderColor = new Scrollbar(10, 300, setColorR));
                addChild(gSliderColor = new Scrollbar(10, 300, setColorG));
                addChild(bSliderColor = new Scrollbar(10, 300, setColorB));

                rBoxColor.setWidth(50);
                gBoxColor.setWidth(50);
                bBoxColor.setWidth(50);

                rBoxColor.x = 282;
                gBoxColor.x = 357;
                bBoxColor.x = 432;
                rBoxColor.y = gBoxColor.y = bBoxColor.y = 25;

//              Let's set them all to RO as the simpliest sync way
                rBoxColor.setEditable(false);
                gBoxColor.setEditable(false);
                bBoxColor.setEditable(false);


                rSliderColor.x = rBoxColor.x + 20;
                gSliderColor.x = gBoxColor.x + 20;
                bSliderColor.x = bBoxColor.x + 20;
                rSliderColor.y = gSliderColor.y = bSliderColor.y = rBoxColor.y + 30;


                cbLockR.x = 301;
                cbLockG.x = 376;
                cbLockB.x = 451;
                cbLockR.y = cbLockG.y = cbLockB.y = 360;
                cbLockR.addListener(lockListenerR);
                cbLockG.addListener(lockListenerG);
                cbLockB.addListener(lockListenerB);
                addChild(cbLockR);
                addChild(cbLockG);
                addChild(cbLockB);

                rColor = application.colorCorrectionColorR;
                gColor = application.colorCorrectionColorG;
                bColor = application.colorCorrectionColorB;
        }


        private function update():void {
                r = Math.max(0, Math.min(r, 1));
                g = Math.max(0, Math.min(g, 1));
                b = Math.max(0, Math.min(b, 1));

                rBoxBright.setContents('' + Math.round(100 * r));
                gBoxBright.setContents('' + Math.round(100 * g));
                bBoxBright.setContents('' + Math.round(100 * b));

                rBoxColor.setContents('' + Math.round(100 * 3 * rColor));
                gBoxColor.setContents('' + Math.round(100 * 3 * gColor));
                bBoxColor.setContents('' + Math.round(100 * 3 * bColor));


                rSliderBright.update(r, 0.08);
                gSliderBright.update(g, 0.08);
                bSliderBright.update(b, 0.08);


                rSliderColor.update(rColor, 0.08);
                gSliderColor.update(gColor, 0.08);
                bSliderColor.update(bColor, 0.08);


                application.setColorSensorCorrection(r, g, b, rColor, gColor, bColor);



                for(var f:int = 0; f < this.application.arrColorNames.length; f++){
                   for(var i:int = 0; i < 4; i++){
                      var arrstrValue:Array = arrColorListFields[this.application.arrColorNames[f]][i].text.split("-");
                      application.arrColorRegion[application.arrColorNames[f]][i*2]   = int(arrstrValue[0]);
                      application.arrColorRegion[application.arrColorNames[f]][i*2+1] = int(arrstrValue[1]);
                   }
                }

        }

        private function rTextChangedBright():void{
                var n:Number = Number(rBoxBright.contents());
                if (n == n) r = n / 100;
                update();
        }
        private function gTextChangedBright():void{
                var n:Number = Number(gBoxBright.contents());
                if (n == n) g = n / 100;
                update();
        }
        private function bTextChangedBright():void{
                var n:Number = Number(bBoxBright.contents());
                if (n == n) b = n / 100;
                update();
        }
        private function rTextChangedColor():void{
                var n:Number = Number(rBoxColor.contents());
                if (n == n) rColor = n / 300;
                update();
        }
        private function gTextChangedColor():void{
                var n:Number = Number(gBoxColor.contents());
                if (n == n) gColor = n / 300;
                update();
        }
        private function bTextChangedColor():void{
                var n:Number = Number(bBoxColor.contents());
                if (n == n) bColor = n / 300;
                update();
        }


        private function setr(n:Number):void { r = n; update() }
        private function setg(n:Number):void { g = n; update() }
        private function setb(n:Number):void { b = n; update() }

        private function setColorR(n:Number):void{
           if(cbLockG.state && cbLockB.state){
              //Other locked, we can noting to do

              rColor = 1 - gColor - bColor;
           }
           else{
              rColor = n;

              if(!cbLockG.state && !cbLockB.state){
                 bColor = gColor = (1 - rColor) / 2;
              }
              else if(cbLockG.state){
                 //Green locked
                 bColor = 1 - rColor - gColor;

                 if(bColor < 0){
                    bColor = 0;
                    rColor = 1 - gColor;
                 }
              }
              else if(cbLockB.state){
                 //Blue locked
                 gColor = 1 - rColor - bColor;

                 if(gColor < 0){
                    gColor = 0;
                    rColor = 1 - bColor;
                 }
              }
           }

           update();
        }
        private function setColorG(n:Number):void{
           if(cbLockR.state && cbLockB.state){
              //Other locked, we can noting to do
              gColor = 1 - rColor - bColor;
           }
           else{
              gColor = n;

              if(!cbLockR.state && !cbLockB.state){
                 rColor = bColor = (1 - gColor) / 2;
              }
              else if(cbLockR.state){
                 //Red locked
                 bColor = 1 - gColor - rColor;

                 if(bColor < 0){
                    bColor = 0;
                    gColor = 1 - rColor;
                 }
              }
              else if(cbLockB.state){
                 //Blue locked
                 rColor = 1 - gColor - bColor;

                 if(rColor < 0){
                    rColor = 0;
                    gColor = 1 - bColor;
                 }
              }
           }

           update();
        }
        private function setColorB(n:Number):void{
           if(cbLockR.state && cbLockG.state){
              //Other locked, we can noting to do
              bColor = 1 - rColor - gColor;
           }
           else{
              bColor = n;

              if(!cbLockR.state && !cbLockG.state){
                 rColor =gColor = (1 - bColor) / 2;
              }
              else if(cbLockR.state){
                 //Red locked
                 gColor = 1 - rColor - bColor;

                 if(gColor < 0){
                    gColor = 0;
                    bColor = 1 - rColor;
                 }
              }
              else if(cbLockG.state){
                 //Green locked
                 rColor = 1 - gColor - bColor;

                 if(rColor < 0){
                    rColor = 0;
                    bColor = 1 - gColor;
                 }
              }
           }

           update();
        }



        private function lockListenerR():void{
           if(cbLockR.state){
              rSliderColor.allowDragging(false);
              rBoxColor.setEditable(false);
           }
           else{
              rSliderColor.allowDragging(true);
//              rBoxColor.setEditable(true);
           }
        }
        private function lockListenerG():void{
           if(cbLockG.state){
              gSliderColor.allowDragging(false);
              gBoxColor.setEditable(false);
           }
           else{
              gSliderColor.allowDragging(true);
//              gBoxColor.setEditable(true);
           }
        }
        private function lockListenerB():void{
           if(cbLockR.state){
              bSliderColor.allowDragging(false);
              bBoxColor.setEditable(false);
           }
           else{
              bSliderColor.allowDragging(true);
//              bBoxColor.setEditable(true);
           }
        }


        private function makeLabel(s:String, fontSize:int, x:int = 0, y:int = 0, bold:Boolean = false):TextField {
                var tf:TextField = new TextField();
                tf.selectable = false;
                tf.defaultTextFormat = new TextFormat(CSS.font, fontSize, CSS.textColor, bold);
                tf.autoSize = TextFieldAutoSize.LEFT;
                tf.text = s;
                tf.x = x;
                tf.y = y;
                addChild(tf);
                return tf;
        }

}}
