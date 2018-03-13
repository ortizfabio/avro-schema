import java.io._
import java.net.URI
import java.nio.file.{Path, Paths}

import org.apache.avro.Schema
import org.apache.avro.compiler.idl.Idl
import org.apache.avro.tool.IdlToSchemataTool

import scala.collection.mutable.ArrayBuffer

/**
  * Created by db2admin on 17/10/21.
  */
object IdlRunner {

  def generate(idlFile: File, outputDirectory: File): Unit = {
    val parser: Idl = new Idl(idlFile)
    val pretty = true
    import scala.collection.JavaConversions._
    for (schema <- parser.CompilationUnit.getTypes) {
      printAvsc(schema, outputDirectory, pretty)
    }
    parser.close()

  }

  @throws[FileNotFoundException]
  private def printAvsc(schema: Schema, outputDirectory: File, pretty: Boolean) = {
    val dirpath = outputDirectory.getAbsolutePath
    val filename = dirpath + "/" + schema.getName + ".avsc"
    val fileOutputStream = new FileOutputStream(filename)
    val printStream = new PrintStream(fileOutputStream)
    printStream.println(schema.toString(pretty))
    printStream.close()
  }

  def runIdl(args: List[String]): Unit = {
    import collection.JavaConversions._
    val idlTool = new IdlToSchemataTool
    idlTool.run(System.in, System.out, System.err, args)
  }

  def matchingFiles(sourceDir: File, extension: String): Array[String] = {

    def find(sourceDir: File, acc: ArrayBuffer[File]): Array[File] = {
      val subDirs: Array[File] = sourceDir.listFiles().filter(f => f.isDirectory)
      val files: Array[File] = sourceDir.listFiles(new FilenameFilter {
        override def accept(dir: File, name: String): Boolean = {
          name.endsWith("avdl")
        }
      })
      acc ++= files
      subDirs.foreach(f => find(f, acc))
      acc.toArray
    }

    val acc = find(sourceDir, ArrayBuffer.empty[File])
    acc.map(f => f.getPath)
  }


  def findFiles(sourceDir: File, extension: String): Array[String] = {
    sourceDir.list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = {
        name.endsWith("avdl")
      }
    })
  }

  /**
    * Arguments:
    * 0 - The directory where the IDL files are.
    * 1 - The directory where the AVSC will be created to.
    *
    * @param args
    */
  def main(args: Array[String]) = {
    val sourceDir = new File(args(0))
    val destDir = new File(args(1))
    val files: Array[String] = sourceDir.isDirectory && destDir.isDirectory match {
      case true => matchingFiles(sourceDir, "avdl")
      case false => {
        Array.empty[String]
      }
    }
    val current: Path = Paths.get(args(1)).toAbsolutePath.normalize
    //   files.toList.foreach(f => generate(new File(s"$sourceDir/$f"),destDir))
    files.foreach(f => {
      runIdl(List(f, args(1)))
    })
  }
}
