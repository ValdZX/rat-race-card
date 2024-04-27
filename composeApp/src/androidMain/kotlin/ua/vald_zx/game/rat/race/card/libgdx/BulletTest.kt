package ua.vald_zx.game.rat.race.card.libgdx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.collision.Collision
import com.badlogic.gdx.physics.bullet.collision.ContactListener
import com.badlogic.gdx.physics.bullet.collision.btBoxShape
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape
import com.badlogic.gdx.physics.bullet.collision.btConeShape
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration
import com.badlogic.gdx.physics.bullet.collision.btDispatcher
import com.badlogic.gdx.physics.bullet.collision.btSphereShape
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ArrayMap
import com.badlogic.gdx.utils.Disposable
import ktx.app.KtxScreen


class BulletTest : KtxScreen {
    inner class MyContactListener : ContactListener() {
        override fun onContactAdded(
            userValue0: Int,
            partId0: Int,
            index0: Int,
            match0: Boolean,
            userValue1: Int,
            partId1: Int,
            index1: Int,
            match1: Boolean
        ): Boolean {
            if (match0) (instances!![userValue0].materials[0][ColorAttribute.Diffuse] as ColorAttribute).color.set(
                Color.WHITE
            )
            if (match1) (instances!![userValue1].materials[0][ColorAttribute.Diffuse] as ColorAttribute).color.set(
                Color.WHITE
            )
            return true
        }
    }

    class MyMotionState : btMotionState() {
        var transform: Matrix4? = null

        override fun getWorldTransform(worldTrans: Matrix4) {
            worldTrans.set(transform)
        }

        override fun setWorldTransform(worldTrans: Matrix4) {
            transform?.set(worldTrans)
        }
    }

    class GameObject(model: Model?, node: String?, constructionInfo: btRigidBodyConstructionInfo?) :
        ModelInstance(model, node), Disposable {
        val body: btRigidBody
        val motionState: MyMotionState = MyMotionState()

        init {
            motionState.transform = transform
            body = btRigidBody(constructionInfo)
            body.motionState = motionState
        }

        override fun dispose() {
            body.dispose()
            motionState.dispose()
        }

        class Constructor(
            val model: Model?,
            val node: String,
            val shape: btCollisionShape,
            mass: Float
        ) :
            Disposable {
            val constructionInfo: btRigidBodyConstructionInfo

            init {
                if (mass > 0f) shape.calculateLocalInertia(mass, localInertia)
                else localInertia[0f, 0f] = 0f
                this.constructionInfo = btRigidBodyConstructionInfo(
                    mass, null,
                    shape, localInertia
                )
            }

            fun construct(): GameObject {
                return GameObject(model, node, constructionInfo)
            }

            override fun dispose() {
                shape.dispose()
                constructionInfo.dispose()
            }

            companion object {
                private val localInertia = Vector3()
            }
        }
    }

    var cam: PerspectiveCamera? = null
    var camController: CameraInputController? = null
    var modelBatch: ModelBatch? = null
    var environment: Environment? = null
    var model: Model? = null
    var instances: Array<GameObject>? = null
    var constructors: ArrayMap<String, GameObject.Constructor>? = null
    var spawnTimer: Float = 0f

    var collisionConfig: btCollisionConfiguration? = null
    var dispatcher: btDispatcher? = null
    var contactListener: MyContactListener? = null
    var broadphase: btBroadphaseInterface? = null
    var dynamicsWorld: btDynamicsWorld? = null
    var constraintSolver: btConstraintSolver? = null

    override fun show() {
        Bullet.init()

        modelBatch = ModelBatch()
        environment = Environment()
        environment?.set(ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f))
        environment?.add(DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f))

        cam = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
            position[3f, 7f] = 10f
            lookAt(0f, 4f, 0f)
            near = 1f
            far = 300f
            update()
        }

        camController = CameraInputController(cam)
        Gdx.input.inputProcessor = camController

        val mb = ModelBuilder()
        mb.begin()
        mb.node().id = "ground"
        val partBuilder = mb.part(
            "ground", GL20.GL_TRIANGLES, (Usage.Position or Usage.Normal).toLong(), Material(
                ColorAttribute.createDiffuse(Color.RED)
            )
        )
        BoxShapeBuilder.build(partBuilder, 5f, 1f, 5f)
        mb.node().id = "sphere"
        mb.part(
            "sphere", GL20.GL_TRIANGLES, (Usage.Position or Usage.Normal).toLong(), Material(
                ColorAttribute.createDiffuse(Color.GREEN)
            )
        )
            .sphere(1f, 1f, 1f, 10, 10)
        mb.node().id = "box"
        mb.part(
            "box", GL20.GL_TRIANGLES, (Usage.Position or Usage.Normal).toLong(), Material(
                ColorAttribute.createDiffuse(Color.BLUE)
            )
        )
            .box(1f, 1f, 1f)
        mb.node().id = "cone"
        mb.part(
            "cone", GL20.GL_TRIANGLES, (Usage.Position or Usage.Normal).toLong(), Material(
                ColorAttribute.createDiffuse(Color.YELLOW)
            )
        )
            .cone(1f, 2f, 1f, 10)
        mb.node().id = "capsule"
        mb.part(
            "capsule", GL20.GL_TRIANGLES, (Usage.Position or Usage.Normal).toLong(), Material(
                ColorAttribute.createDiffuse(Color.CYAN)
            )
        )
            .capsule(0.5f, 2f, 10)
        mb.node().id = "cylinder"
        mb.part(
            "cylinder", GL20.GL_TRIANGLES, (Usage.Position or Usage.Normal).toLong(),
            Material(ColorAttribute.createDiffuse(Color.MAGENTA))
        ).cylinder(1f, 2f, 1f, 10)
        model = mb.end()

        constructors = ArrayMap(
            String::class.java,
            GameObject.Constructor::class.java
        )
        constructors?.put(
            "ground",
            GameObject.Constructor(model, "ground", btBoxShape(Vector3(2.5f, 0.5f, 2.5f)), 0f)
        )
        constructors?.put(
            "sphere",
            GameObject.Constructor(model, "sphere", btSphereShape(0.5f), 1f)
        )
        constructors?.put(
            "box",
            GameObject.Constructor(model, "box", btBoxShape(Vector3(0.5f, 0.5f, 0.5f)), 1f)
        )
        constructors?.put("cone", GameObject.Constructor(model, "cone", btConeShape(0.5f, 2f), 1f))
        constructors?.put(
            "capsule",
            GameObject.Constructor(model, "capsule", btCapsuleShape(.5f, 1f), 1f)
        )
        constructors?.put(
            "cylinder", GameObject.Constructor(
                model, "cylinder", btCylinderShape(Vector3(.5f, 1f, .5f)),
                1f
            )
        )

        collisionConfig = btDefaultCollisionConfiguration()
        dispatcher = btCollisionDispatcher(collisionConfig)
        broadphase = btDbvtBroadphase()
        constraintSolver = btSequentialImpulseConstraintSolver()
        dynamicsWorld =
            btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig)
        dynamicsWorld?.gravity = Vector3(0f, -10f, 0f)
        contactListener = MyContactListener()

        instances = Array()
        val obj = constructors!!["ground"].construct()
        obj.body.collisionFlags =
            obj.body.collisionFlags or btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT
        instances?.add(obj)
        dynamicsWorld?.addRigidBody(obj.body)
        obj.body.contactCallbackFlag = GROUND_FLAG.toInt()
        obj.body.contactCallbackFilter = 0
        obj.body.activationState = Collision.DISABLE_DEACTIVATION
    }

    fun spawn() {
        val obj = constructors!!.values[1 + MathUtils.random(
            constructors!!.size - 2
        )].construct()
        obj.transform.setFromEulerAngles(
            MathUtils.random(360f),
            MathUtils.random(360f),
            MathUtils.random(360f)
        )
        obj.transform.trn(MathUtils.random(-2.5f, 2.5f), 9f, MathUtils.random(-2.5f, 2.5f))
        obj.body.proceedToTransform(obj.transform)
        obj.body.userValue = instances!!.size
        obj.body.collisionFlags =
            obj.body.collisionFlags or btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK
        instances?.add(obj)
        dynamicsWorld?.addRigidBody(obj.body)
        obj.body.contactCallbackFlag = OBJECT_FLAG.toInt()
        obj.body.contactCallbackFilter = GROUND_FLAG.toInt()
    }

    var angle: Float = 0f
    var speed: Float = 90f

    override fun render(delta: Float) {
        angle = (angle + delta * speed) % 360f
        instances!![0].transform.setTranslation(0f, MathUtils.sinDeg(angle) * 2.5f, 0f)

        dynamicsWorld?.stepSimulation(delta, 5, 1f / 60f)

        if ((delta.let { spawnTimer -= it; spawnTimer }) < 0) {
            spawn()
            spawnTimer = 1.5f
        }

        camController?.update()

        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        modelBatch?.begin(cam)
        modelBatch?.render(instances, environment)
        modelBatch?.end()
    }

    override fun dispose() {
        for (obj in instances!!) obj.dispose()
        instances?.clear()

        for (ctor in constructors!!.values()) ctor.dispose()
        constructors?.clear()

        dynamicsWorld?.dispose()
        constraintSolver?.dispose()
        broadphase?.dispose()
        dispatcher?.dispose()
        collisionConfig?.dispose()

        contactListener?.dispose()

        modelBatch?.dispose()
        model?.dispose()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(width: Int, height: Int) {
    }

    companion object {
        const val GROUND_FLAG: Short = (1 shl 8).toShort()
        const val OBJECT_FLAG: Short = (1 shl 9).toShort()
        const val ALL_FLAG: Short = -1
    }
}
