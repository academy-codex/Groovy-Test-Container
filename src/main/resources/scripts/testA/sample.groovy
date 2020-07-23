package scripts.testA

def a = "Hello World!"
println(a)

println("Printing the input value for the key")
def input = dataMap.get("abc")
println(input)

calculatedOutput = input.get("name")+", "+input.get("age")


