/**
 * ZParse - Javascript Library for Template Parsing
 * @author Rizqi Ahmad
 *
 * Copyright (c) 2007 Rizqi Ahmad
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Main Object of ZParse. Acting as a class and a namespace at the same time
 * @param {Object} implementation
 * @example
 * 	<code>
 * 	var parser1 = new ZParse();
 * 	var parser2 = new ZParse(customImplementation);
 *
 * 	parser1.parse(templateString1);
 * 	parser2.parse(templateString2);
 *
 *  var result1 = parser1.process(data);
 *  var result2 = parser2.process(data);
 *	</code>
 */
var ZParse = function(implementation) {
	/*
	 * @type {Object} Rule Object
	 */
	this.implementation = implementation;

	/*
	 * @type {String} Function-Header for the template
	 */
	this.header = 	[	'var $text = [];',
						'var _write = function(text) {',
							'$text.push((typeof text == "number")?text:(text||""));',
						'};',
                        'var top = $data;',
						'with($data){ '
						].join('');

	/*
	 * @type {String} Function-Footer for the template
	 */
	this.footer = '}return $text.join("");';

	/*
	 * @type {Array} Characters to be escaped
	 */
	this.escapeChars = ['\\', '\'', '"', ['\n','\\n'], ['\t','\\t'], ['\r', '\\r']];
};
/////////////////////////////////////////////////////////////////////////////////////////////////////
/*
 *  @type {Object} Default rule object to be used
 */
ZParse.implementation = {};
/////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Parse template string to template function
 * @param {String} Template string to be parsed
 * @return {Boolean} Parsing status, false if failed, true if succeeded
 * @example
 * 	<code>
 * 	var parser = new ZParse;
 * 	parser.parse(myTemplateString);
 *  </code>
 */
ZParse.prototype.parse = function(source) {

	this.sourceArray = ZParse.parseToArray(source, this.implementation);
	this.sourceTree = ZParse.parseToTree(this.sourceArray.all, this.implementation);
	this.functionText = ZParse.parseToScript(this.sourceTree, this.escapeChars, this.implementation, this);

	try {
		this.functionScript = new Function('$data', this.header + this.functionText + this.footer);
        return true;
    } catch(e){
		this.error = e;
		return false;
	}

};


ZParse.clean = function (str) {
    return str.replace(/[\n\r\t](?!.*?<)/gmi,"").replace(/(<\w+?|\"+?)\s{2,}/g,"$1 ");
};

/////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Process created Template with data
 * @param {Object} data that will be used when executing function. You can access data within template
 * @param {Object} 'this'-object to be binded in template
 * @return {String} text result
 * @example
 *  <code>
 *  var parser = new ZParse;
 *  var string = 'my name ist <$name>. Im <$age> years old.'
 *
 *  parser.parse(string);
 *  var result = parser.process({name:'Rizqi', age:17});
 *  // result: "my name is Rizqi. Im 17 years old."
 *  </code>
 */
ZParse.prototype.process = function(data, bind) {
	if(bind)
		return ZParse.clean(this.functionScript.apply(bind, [data]));
	else
		return ZParse.clean(this.functionScript(data));
};

/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////    ZParse Global Functions     ////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Escape characters. Needed for parsing template.
 * @param {String} string containing characters to be escaped
 * @param {Array} escape characters list
 * @return {String} parsed string
 * @example
 *  <code>
 *  var escaped = ZParse.escape("lesser < more >", ['<','>']);
 *  // produce: 'lesser \< more \>'
 *  </code>
 */
ZParse.escape = function(source, list) {
	for(var i=0; i<list.length; i++) {
		if(list[i] instanceof Array)
			source =  source.replace(new RegExp(list[i][0],'gi'), list[i][1]);
		else
			source = source.replace(new RegExp('\\'+list[i],'gi'), '\\'+list[i]);
	}
	return source;
};
/////////////////////////////////////////////////////////////////////////////////////////////////////
/*
Function: parseToArray

Parse template string to array

Parameters:
	source -	String, template source
	imp    -	Object, implementation object

Return:
	Array, template array

Example:
	(start code)

	(end)
*/
/**
 * Parse template string to array
 * @param {String} source
 * @param {Object} imp
 */
ZParse.parseToArray = function(source, imp) {
	var opener, closer, delimiter;
	var text = [],
		tags = [],
		all = [];

	while(source) {
		for(var i in imp) {
			if(!delimiter || source.indexOf(imp[delimiter].opener) == -1)
				delimiter = i;
			if(source.indexOf(imp[i].opener) != -1)
				if(source.indexOf(imp[delimiter].opener) > source.indexOf(imp[i].opener))
					delimiter = i;
		}

		opener = source.indexOf(imp[delimiter].opener);
		closer = source.indexOf(imp[delimiter].closer) + imp[delimiter].closer.length;

		if(opener != -1) {
			text.push(source.substring(0,opener));
			tags.push(source.substring(opener,closer));
			source = source.substring(closer);
		} else {
			text.push(source);
			source = '';
		}
	}

	for(var i=0; i<text.length; i++) {
		all.push(text[i]);
		if(tags[i])
			all.push(tags[i]);
	}

	return {text:text, tags:tags, all:all};
};

/////////////////////////////////////////////////////////////////////////////////////////////////////
ZParse.parseArguments = function(source, expr) {

	var args = ZParse.parseToArray(expr, {expr:{opener:'\{',closer:'\}'}}).tags;
	expr = ZParse.escape(expr, ['(' ,')' ,'[' ,']', ',', '.', '<', '>', '*', '$', '@']);
	for(var i=0; i<args.length; i++) {
		expr = expr.replace(args[i],'(.*)');
		args[i] = args[i].replace('\{', '').replace('\}', '');
	}
	var matches = source.match(new RegExp(expr));

	var result = {};
	if(matches)
		for(var i=0; i<args.length; i++)
			result[args[i]] = matches[i+1];

	return result;
};

/////////////////////////////////////////////////////////////////////////////////////////////////////
ZParse.parseTag = function(source, imp) {
	// Parse Tag
	var delimiter;
	for(var i in imp)
		if(source.indexOf(imp[i].opener) == 0) {
			delimiter = i;
			break;
		}
	if(!delimiter)
		return false;
	source =  source.substring(imp[delimiter].opener.length, source.indexOf(imp[delimiter].closer));

	// Parse tag name
	var tagname = '';
	var closer = '';
	if(imp[delimiter].tags) {
		var tagArray = [];
		for(var i in imp[delimiter].tags)
			tagArray.push(i);
		var regex = new RegExp('^(\/){0,1}('+tagArray.join('|')+')\\\s*(.*)');
		var res =  source.match(regex);
		if(!res)
			return false;
		closer = res[1]?true:false;
		tagname = res[2];
		source = res[3];
	}

	// Parse tag type
	if(tagname) {
		if(imp[delimiter].tags[tagname].type == 'single' && closer)
			return false;
		if(imp[delimiter].tags[tagname].type == 'block' && closer)
			return { delimiter:delimiter, tagname:tagname, closer:true};
	}

	// Parse arguments
	var args = {};
	if(tagname && imp[delimiter].tags[tagname].arguments) {
		args = ZParse.parseArguments(source, imp[delimiter].tags[tagname].arguments);
    } else if(!tagname && imp[delimiter].arguments) {
		args = ZParse.parseArguments(source, imp[delimiter].arguments);
    }
	return {delimiter:delimiter, tagname:tagname, source:source, arguments:args};
};

/////////////////////////////////////////////////////////////////////////////////////////////////////
ZParse.parseToTree = function(array, imp) {
	var obj, res, current, nr = 0;
	var doc = {
		isDocument:true,
		innerSource: array.join(''),
		children: []
	};

	var addChild = function(parent,child) {
		child.nr = parent.children.length;
		parent.children.push(child);
	};

	current = doc;
	for(var i=0; i<array.length; i++) {
		res = ZParse.parseTag(array[i], imp);
		if(!res) {
			if(array[i]) {
				array[i].parent = current;
				addChild(current, array[i]);
			}
		} else {
			obj = {};

			obj.i = i;
			obj.tagname = res.tagname;
			obj.delimiter = res.delimiter;
			obj.arguments = res.arguments;
			obj.argSource = res.source;
			obj.parent = current;

			if(res.tagname && imp[res.delimiter].tags[res.tagname].noTextBefore
				&& !res.closer && typeof current.children[current.children.length-1] == 'string')
					current.children.pop();

			if(res.tagname && imp[res.delimiter].tags[res.tagname].type == 'block'){
				if(!res.closer) {
					addChild(current, obj);
					current = obj;
					current.children = [];
				} else if(current.tagname == res.tagname) {
					current.innerSource = '';
					for(var j=current.i+1; j<i; j++){
						current.innerSource += array[j];
					}
					current = current.parent;
				}
			} else
				addChild(current, obj);
		}
	}

	return doc;
};

/////////////////////////////////////////////////////////////////////////////////////////////////////
ZParse.parseToScript = function(tree, escape, imp, caller) {
	if(typeof tree == 'string')
		return '_write(\''+ZParse.escape(tree, escape)+'\');';

	var result, content = [];

	if(tree.children)
		for(var i=0; i<tree.children.length; i++)
			content.push(ZParse.parseToScript(tree.children[i], escape, imp, caller));
	if(!tree.isDocument) {
		if(tree.tagname)
			return imp[tree.delimiter].tags[tree.tagname].handler(tree, content.join(''), caller);
		else
			return imp[tree.delimiter].handler(tree, content.join(''), caller);

	} else
		return content.join('');
};
/////////////////////////////////////////////////////////////////////////////////////////////////////

ZParse.parseXMLToJSON = function(xml) {
	var result;
	if(xml.childNodes && xml.childNodes.length == 1 && xml.childNodes[0].nodeName == "#text") {
		result = xml.childNodes[0].nodeValue;
	} else{
		result = {};
		for(var i=0; i<xml.childNodes.length; i++) {
			if(result[xml.childNodes[i].nodeName]) {
				if(!(result[xml.childNodes[i].nodeName] instanceof Array))
					result[xml.childNodes[i].nodeName] = [result[xml.childNodes[i].nodeName]];
				result[xml.childNodes[i].nodeName].push(ZParse.parseXMLToJSON(xml.childNodes[i]));
			}else if(xml.childNodes[i].nodeName.indexOf('#') == -1)
				result[xml.childNodes[i].nodeName] = ZParse.parseXMLToJSON(xml.childNodes[i]);
		}
	}

	if(xml.attributes)
		for(var i=0; i<xml.attributes.length; i++)
			result['@'+xml.attributes[i].nodeName] = xml.attributes[i].nodeValue;

	return result;
}


var Implementation = {
	statement: {
		opener: '<?',
		closer: '?>',

		tags: {

			'foreach': {
				arguments: '{element} in {object}',
				type: 'block',
				handler: function(tree, content, caller) {
					var element = tree.arguments.element;
					var object = tree.arguments.object;

					//Check whether there are else tag after this tag, if yes, (if) tag will be included
					var cond = (tree.parent.children[tree.nr+1] && tree.parent.children[tree.nr+1].tagname == 'else');
					var iff = ['if( (',object,' instanceof Array && ',object,'.length > 0) || ',
								'(!(',object,' instanceof Array) && ',object,') ) {'].join('');
					return [
						cond?iff:'',
							'var ',element,';',
							'if(',object,' instanceof Array) {',
								'for(var ',element,'_index=0; ',element,'_index<',object,'.length; ',element,'_index++) {',
									element,' = ',object,'[',element,'_index];',
									content,
								'}',
							'} else {',
								'for (var ',element,'_index in ',object,') {',
									element,' = ',object,'[',element,'_index];',
									content,
								'}',
							'}',
						cond?'}':''
					].join('');
				}
			},

			'for': {
				arguments: '{element} in {object}',
				type: 'block',
				handler: function(tree, content, caller) {
					var element = tree.arguments.element;
					var object = tree.arguments.object;

					//Check whether there are else tag after this tag, if yes, (if) tag will be included
					var cond = (tree.parent.children[tree.nr+1] && tree.parent.children[tree.nr+1].tagname == 'else');
					var iff = ['if( (',object,' instanceof Array && ',object,'.length > 0) || ',
								'(!(',object,' instanceof Array) && ',object,') ) {'].join('');
					return [
						cond?iff:'',
							'if(',object,' instanceof Array) {',
								'for(var ',element,'=0; ',element,'<',object,'.length; ',element,'++) {',
									content,
								'}',
							'} else {',
								'for(var ',element,' in ',object,') {',
									content,
								'}',
							'}',
						cond?'}':''
					].join('');
				}
			},

			'if': {
				type: 'block',

				handler: function(tree, content, caller) {


					var condition = tree.argSource;

					return [
                        

                        'if(',condition,') {',
							content,
						'}'
					].join('');
				}
			},

			'elseif': {
				type: 'block',
				noTextBefore: true,

				handler: function(tree, content, caller) {
					var condition = tree.argSource;

					return [
						'else if(',condition,') {',
							content,
						'}'
					].join('');
				}
			},

			'else': {
				type: 'block',
				noTextBefore: true,

				handler: function(tree, content, caller) {
					return [
						'else {',
							content,
						'}'
					].join('');
				}
			},

            

			'macro': {
				arguments: '{name}({args})',
				type: 'block',
				handler: function(tree, content, caller) {
					var name = tree.arguments.name;
					var args = tree.arguments.args;
					var point = (name.indexOf('.') > 0);
					return [
						point?'':'var ', name,' = function(',args,') {',
                                'var top = ' + args + ";",
                                'with (' + args + ') {',
                                    'var $text = [];',
                                    'var _write = function(text) {',
                                        '$text.push((typeof text == "number")?text:(text||""));',
                                    '};',
                                    content,
                                '}',
                                'return $text.join("");',
						    '};'
					    ].join('');
				}
			},

			'cdata': {
				type: 'block',
				handler: function(tree, content, caller) {
					return '_write(\''+ZParse.escape(tree.innerSource, caller.escapeChars)+'\');';
				}
			}
		}
	},

	print: {
		opener: '${',
		closer: '}',
		handler: function(tree, content, caller) {
			return '_write('+tree.argSource+');';
		}
	},

	alternatePrint: {
		opener: '<$',
		closer: ':>',
		handler: function(tree, content, caller) {
			return '_write('+tree.argSource+');';
		}
	},

	script: {
		opener: '<%',
		closer: '%\>',
		handler: function(tree, content, caller) {
			return tree.argSource;
		}
	}
};