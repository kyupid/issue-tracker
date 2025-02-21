import styled from 'styled-components';

const Markdown = ({ string }: { string: string }) => {
  const pipe =
    (...fns: Function[]) =>
    (value: any) =>
      fns.reduce((acc, fn) => fn(acc), value);
  const checkHeader = (str: string) => {
    const check = str.match(/^[\#]+ |^[\>] /gi);
    if (check)
      return { key: check[0], str: str.replace(/^[\#]+ |^[\>] /gi, '') };
    else return { key: null, str: str };
  };

  const headerParser = (key: string | null, str: string | JSX.Element) => {
    switch (key) {
      case '# ':
        return <h1>{str}</h1>;
      case '## ':
        return <h2>{str}</h2>;
      case '### ':
        return <h3>{str}</h3>;
      case '#### ':
        return <h4>{str}</h4>;
      case '##### ':
        return <h5>{str}</h5>;
      case '###### ':
        return <h6>{str}</h6>;
      case '> ':
        return <QuotationStyle>{str}</QuotationStyle>;
      case null:
        return <span>{str}</span>;
    }
  };

  const emphasisParser = (str: string) => {
    const arr = str.match(/(\*{2,5})(.*?)\1/g);
    let res = [];
    if (!arr) return str;
    let sp = str.replace(/(\*{2,5})(.*?)\1/g, '\n').split('\n');
    let j = 0;

    for (let i = 0; i < sp.length; i++) {
      if (!sp[i].length) {
        res.push(<b>{arr[j]?.slice(2, -2)}</b>);
        j++;
      } else res.push(sp[i]);
    }
    return <>{res}</>;
  };

  // const checkHeader = (str: string) => [
  //   str.match(/^[\#]+ |^[\>] /gi)?[0],
  //   str.replace(/^[\#]+ |^[\>] /gi, ''),
  // ];
  return (
    <>
      {string
        .split('\n')
        .map((v) =>
          headerParser(checkHeader(v).key, emphasisParser(checkHeader(v).str))
        )} 
    </>
  );
};

export default Markdown;

const QuotationStyle = styled.div`
  border-left: 4px solid #39a6a3;
  background-color: #deedf0;
  width: 100%;
  padding: 1rem;
`;

