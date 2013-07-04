require 'java'
Java::com.beartronics.jschema.JSchema.main(nil)
$app = Java::com.beartronics.jschema.JSchema.app
$sms = $app.sms
$vec2 = Java::org.jbox2d.common.Vec2
java_import "org.jbox2d.common.Vec2"
java_import "com.beartronics.jschema.JSchema"
$h1 = $sms.hand1
$h2 = $sms.hand2



