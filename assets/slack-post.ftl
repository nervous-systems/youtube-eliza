## Simplified a little from https://gist.github.com/ryanray/668022ad2432e38493df
#set($rawAPIData = $input.path('$'))

#set($tokenisedAmpersand = $rawAPIData.split("&"))
#set($tokenisedEquals = [])

#foreach($kvPair in $tokenisedAmpersand)
 #set($countEquals = $kvPair.length() - $kvPair.replace("=", "").length())
 #if ($countEquals == 1)
  #set($kvTokenised = $kvPair.split("="))
  #if (0 < $kvTokenised[0].length())
   #set($devNull = $tokenisedEquals.add($kvPair))
  #end
 #end
#end

{
#foreach($kvPair in $tokenisedEquals)
  #set($kvTokenised = $kvPair.split("="))
 "$util.urlDecode($kvTokenised[0])" : #if(0 < $kvTokenised[1].length())"$util.urlDecode($kvTokenised[1])"#{else}""#end#if($foreach.hasNext),#end
#end
}
