export interface ReceiptData {
  id: string;
  amount: string;
  operation: string;
  description: string;
  date: string;
}

const escapePdfText = (value: string): string =>
  value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[\\()]/g, '\\$&');

const createPdf = (lines: string[]): Blob => {
  const content = lines
    .map((line, index) =>
      `BT /F1 ${index === 0 ? '20' : '11'} Tf 50 ${790 - index * 28} Td (${escapePdfText(line)}) Tj ET`
    )
    .join('\n');

  const objects = [
    '<< /Type /Catalog /Pages 2 0 R >>',
    '<< /Type /Pages /Kids [3 0 R] /Count 1 >>',
    '<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>',
    '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>',
    `<< /Length ${content.length} >>\nstream\n${content}\nendstream`,
  ];

  let pdf = '%PDF-1.4\n';
  const offsets: number[] = [];

  objects.forEach((object, index) => {
    offsets.push(pdf.length);
    pdf += `${index + 1} 0 obj\n${object}\nendobj\n`;
  });

  const xrefOffset = pdf.length;
  pdf += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`;
  pdf += offsets
    .map((offset) => `${offset.toString().padStart(10, '0')} 00000 n \n`)
    .join('');
  pdf += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xrefOffset}\n%%EOF`;

  return new Blob([pdf], { type: 'application/pdf' });
};

export const downloadPaymentReceipt = ({
  id,
  amount,
  operation,
  description,
  date,
}: ReceiptData): void => {
  const pdf = createPdf([
    'Digital Money House',
    'Comprobante de operacion',
    `Monto: ${amount}`,
    `Operacion: ${operation}`,
    `Descripcion: ${description}`,
    `Fecha: ${date}`,
    `Numero de operacion: ${id}`,
    'Comprobante generado por Digital Money House.',
  ]);
  const url = URL.createObjectURL(pdf);
  const link = document.createElement('a');

  link.href = url;
  link.download = `comprobante-${id}.pdf`;
  link.click();
  URL.revokeObjectURL(url);
};
